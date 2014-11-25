package org.mechaverse.simulation.common.circuit.analysis;

import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
import org.mechaverse.simulation.common.circuit.CircuitUtil;
import org.mechaverse.simulation.common.circuit.generator.CircuitGeneratorCLI;
import org.mechaverse.simulation.common.opencl.OpenClCircuitSimulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A command line utility for performing circuit analysis.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class CircuitAnalysisCLI {

  private static final int THREAD_COUNT = 4;

  private static class CircuitAnalysisTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CircuitAnalysisTask.class);

    private int sampleSize;
    private int maxIterationCount;
    private Circuit circuit;
    private PrintStream out;

    public CircuitAnalysisTask(int sampleSize, int maxIterationCount, Circuit circuit,
        PrintStream out) {
      this.sampleSize = sampleSize;
      this.maxIterationCount = maxIterationCount;
      this.circuit = circuit;
      this.out = out;
    }

    @Override
    public void run() {
      logger.debug("%s - Started", Thread.currentThread().getName());
      try (OpenClCircuitSimulator simulator = new OpenClCircuitSimulator(1, 1, 1, circuit)) {
        for (int cnt = 1; cnt <= sampleSize; cnt++) {
          CircuitAnalyzer circuitAnalyzer = new CircuitAnalyzer(circuit);
          int[] circuitState =
              CircuitUtil.randomState(simulator.getCircuitStateSize(), new Well19937c());
          simulator.setCircuitState(0, circuitState);

          int iteration = 1;
          do {
            simulator.update();
            simulator.getCircuitState(0, circuitState);
            circuitAnalyzer.update(iteration, circuitState);
            iteration++;
          } while (circuitAnalyzer.getCycleLength() == 0 && iteration < maxIterationCount);

          long bitsPerState = CircuitUtil.stateSizeInBytes(circuitState.length) * 8;
          long elementOutputBitsPerState =
              CircuitUtil.stateSizeInBytes(circuitAnalyzer.getElementOutputStateSize()) * 8;
          int cycleLength = circuitAnalyzer.getCycleLength();

          int avgStateDiff = circuitAnalyzer.getAverageStateDifference();
          int avgSetBitCount = circuitAnalyzer.getAverageSetBitCount();
          synchronized (out) {
            out.printf("%d,%d,%d,%d,%d,%d%n",
                circuitAnalyzer.getCycleStartIteration(),
                cycleLength > 0 ? cycleLength : maxIterationCount,
                avgStateDiff,
                avgSetBitCount,
                elementOutputBitsPerState,
                bitsPerState);
            System.out.printf("%s - %d/%d samples complete%n",
                Thread.currentThread().getName(), cnt, sampleSize);
          }
        }
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    Options options = buildOptions();

    try {
      CommandLineParser parser = new GnuParser();
      CommandLine cmd = parser.parse(options, args);

      String inputFilename = cmd.getOptionValue('i');
      String outputFilename = cmd.getOptionValue('o');
      int sampleSize = Integer.parseInt(cmd.getOptionValue("n"));
      int maxIterationCount = Integer.parseInt(cmd.getOptionValue("maxIterations"));

      Circuit circuit = CircuitReader.read(new FileInputStream(inputFilename));

      PrintStream out = System.out;
      if (outputFilename != null) {
        out = new PrintStream(outputFilename);
      }

      out.println("cycleStartIteration,cycleLength,avgDifference,avgSetBitCount,"
          + "elementOutputBitsPerState,bitsPerState");

      ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);
      for(int cnt = 1; cnt <= THREAD_COUNT; cnt++) {
        service.submit(new CircuitAnalysisTask(
            sampleSize / THREAD_COUNT, maxIterationCount, circuit, out));
        Thread.sleep(1*1000);
      }

      service.shutdown();
      while(!service.awaitTermination(1, TimeUnit.SECONDS));

      out.close();
    } catch (NumberFormatException | ParseException ex) {
      System.out.println(ex.getMessage());
      System.out.println();
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(CircuitGeneratorCLI.class.getName(), options);
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
