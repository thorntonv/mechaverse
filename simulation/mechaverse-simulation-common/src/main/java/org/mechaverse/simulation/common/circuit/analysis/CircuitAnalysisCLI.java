package org.mechaverse.simulation.common.circuit.analysis;

import java.io.FileInputStream;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.CircuitReader;
import org.mechaverse.simulation.common.circuit.CircuitSimulator;
import org.mechaverse.simulation.common.circuit.CircuitUtil;
import org.mechaverse.simulation.common.circuit.generator.CircuitGeneratorCLI;
import org.mechaverse.simulation.common.opencl.OpenClCircuitSimulator;

/**
 * A command line utility for performing circuit analysis.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class CircuitAnalysisCLI {

  // TODO(thorntonv): Allow these to be provided as command line arguments.
  private static final int SAMPLE_SIZE = 10;
  private static final int MAX_ITERATION_COUNT = 15000;

  public static void main(String[] args) throws Exception {
    Options options = buildOptions();

    try {
      CommandLineParser parser = new GnuParser();
      CommandLine cmd = parser.parse(options, args);

      String inputFilename = cmd.getOptionValue('i');
      String outputFilename = cmd.getOptionValue('o');

      Circuit circuit = CircuitReader.read(new FileInputStream(inputFilename));
      ;

      PrintStream out = System.out;
      if (outputFilename != null) {
        out = new PrintStream(outputFilename);
      }

      try(OpenClCircuitSimulator simulator = new OpenClCircuitSimulator(1, 1, 1, circuit)) {
        performAnalysis(SAMPLE_SIZE, MAX_ITERATION_COUNT, simulator, out);
      } finally {
        out.close();
      }
    } catch (ParseException ex) {
      System.out.println(ex.getMessage());
      System.out.println();
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(CircuitGeneratorCLI.class.getName(), options);
    }
  }

  private static void performAnalysis(int sampleSize, int maxIterationCount,
      CircuitSimulator simulator, PrintStream out) {
    for (int cnt = 1; cnt <= sampleSize; cnt++) {
      CircuitAnalyzer circuitAnalyzer = new CircuitAnalyzer();
      int[] circuitState =
          CircuitUtil.randomState(simulator.getCircuitStateSize(), new Well19937c());
      simulator.setCircuitState(0, circuitState);

      int iteration = 1;
      do {
        simulator.update();

        circuitState = new int[circuitState.length];
        simulator.getCircuitState(0, circuitState);
        circuitAnalyzer.update(iteration, circuitState);
        iteration++;
      } while (circuitAnalyzer.getCycleLength() == 0 && iteration < maxIterationCount);

      out.printf("%d,%d,%d%n", circuitAnalyzer.getCycleStartIteration(),
          circuitAnalyzer.getCycleLength(), circuitAnalyzer.getAverageStateDifference());

      System.gc();
    }
  }

  private static final Options buildOptions() {
    Options options = new Options();

    OptionGroup group = new OptionGroup();
    group.addOption(new Option("i", "input", true, "Circuit XML file name"));
    group.setRequired(true);
    options.addOption(new Option("o", "output", true, "Output file name"));

    options.addOptionGroup(group);
    return options;
  }
}
