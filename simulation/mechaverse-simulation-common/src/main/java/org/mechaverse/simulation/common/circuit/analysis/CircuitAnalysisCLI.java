package org.mechaverse.simulation.common.circuit.analysis;

import java.io.FileInputStream;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.CircuitReader;
import org.mechaverse.simulation.common.circuit.CircuitSimulator;
import org.mechaverse.simulation.common.circuit.CircuitUtil;
import org.mechaverse.simulation.common.circuit.generator.CircuitGeneratorCLI;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModelBuilder;
import org.mechaverse.simulation.common.opencl.OpenClCircuitSimulator;

/**
 * A command line utility for performing circuit analysis.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class CircuitAnalysisCLI {

  public static void main(String[] args) throws Exception {
    Options options = buildOptions();

    try {
      CommandLineParser parser = new GnuParser();
      CommandLine cmd = parser.parse(options, args);

      String inputFilename = cmd.getOptionValue('i');
      String outputFilename = cmd.getOptionValue('o');
      int numSamples = Integer.parseInt(cmd.getOptionValue("n"));
      int maxIterations = Integer.parseInt(cmd.getOptionValue("maxIterations"));

      Circuit circuit = CircuitReader.read(new FileInputStream(inputFilename));

      PrintStream out = System.out;
      if (outputFilename != null) {
        out = new PrintStream(outputFilename);
      }

      out.println("cycleStartIteration,cycleLength,avgDifference,avgSetBitCount,"
          + "elementOutputBitsPerState,bitsPerState");

      try(OpenClCircuitSimulator simulator = new OpenClCircuitSimulator(1, 1, 1, circuit)) {
        performAnalysis(numSamples, maxIterations, circuit, simulator, out);
      } finally {
        out.close();
      }
    } catch (NumberFormatException | ParseException ex) {
      System.out.println(ex.getMessage());
      System.out.println();
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(CircuitGeneratorCLI.class.getName(), options);
    }
  }

  private static void performAnalysis(int sampleSize, int maxIterationCount,
      Circuit circuit, CircuitSimulator simulator, PrintStream out) {
    for (int cnt = 1; cnt <= sampleSize; cnt++) {
      CircuitAnalyzer circuitAnalyzer = new CircuitAnalyzer();
      int[] circuitState =
          CircuitUtil.randomState(simulator.getCircuitStateSize(), new Well19937c());
      simulator.setCircuitState(0, circuitState);

      CircuitSimulationModel circuitModel = CircuitSimulationModelBuilder.build(circuit);

      int iteration = 1;
      do {
        simulator.update();

        circuitState = new int[circuitState.length];
        simulator.getCircuitState(0, circuitState);
        circuitAnalyzer.update(iteration, circuitState);
        iteration++;
      } while (circuitAnalyzer.getCycleLength() == 0 && iteration < maxIterationCount);

      long bitsPerState = CircuitUtil.stateSizeInBytes(circuitState.length) * 8;
      long elementOutputBitsPerState =
          CircuitUtil.stateSizeInBytes(circuitModel.getElementOutputStateSize()) * 8;
      int cycleLength = circuitAnalyzer.getCycleLength();
      out.printf("%d,%d,%d,%d,%d,%d%n",
          circuitAnalyzer.getCycleStartIteration(),
          cycleLength > 0 ? cycleLength : maxIterationCount,
          circuitAnalyzer.getAverageStateDifference(),
          circuitAnalyzer.getAverageSetBitCount(),
          circuitModel.getElementOutputStateSize(),
          elementOutputBitsPerState,
          bitsPerState);

      System.gc();
    }
  }

  private static final Options buildOptions() {
    Options options = new Options();

    Option inputOption = new Option("i", "input", true, "Circuit XML file name");
    inputOption.setRequired(true);
    options.addOption(inputOption);

    Option sampleSizeOption = new Option("n", true, "Sample size");
    sampleSizeOption.setRequired(true);
    options.addOption(sampleSizeOption);

    Option maxIterationsOption = new Option("maxIterations", true, "Maximum number of iterations");
    maxIterationsOption.setRequired(true);
    options.addOption(maxIterationsOption);

    options.addOption("o", "output", true, "Output file name");

    return options;
  }
}
