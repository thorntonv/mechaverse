package org.mechaverse.simulation.common.cellautomaton.analysis;

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
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorReader;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulationUtil;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.opencl.OpenClCellularAutomatonSimulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A command line utility for performing cellular automaton analysis.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class CellularAutomatonAnalysisCLI {

  private static final int THREAD_COUNT = 4;

  private static class AnalysisTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisTask.class);

    private int sampleSize;
    private int maxIterationCount;
    private CellularAutomatonDescriptor descriptor;
    private final PrintStream out;

    public AnalysisTask(int sampleSize, int maxIterationCount,
        CellularAutomatonDescriptor descriptor, PrintStream out) {
      this.sampleSize = sampleSize;
      this.maxIterationCount = maxIterationCount;
      this.descriptor = descriptor;
      this.out = out;
    }

    @Override
    public void run() {
      logger.debug("%s - Started", Thread.currentThread().getName());
      CellularAutomatonSimulatorConfig config = new CellularAutomatonSimulatorConfig.Builder()
          .setDescriptor(descriptor)
          .build();
      try (OpenClCellularAutomatonSimulator simulator =
          new OpenClCellularAutomatonSimulator(config)) {
        for (int cnt = 1; cnt <= sampleSize; cnt++) {
          CellularAutomatonAnalyzer analyzer = new CellularAutomatonAnalyzer(descriptor);
          int[] state = CellularAutomatonSimulationUtil.randomState(
              simulator.getAutomatonStateSize(), new Well19937c());
          simulator.setAutomatonState(0, state);

          int iteration = 1;
          do {
            simulator.update();
            simulator.getAutomatonState(0, state);
            analyzer.update(iteration, state);
            iteration++;
          } while (analyzer.getCycleLength() == 0 && iteration < maxIterationCount);

          long bitsPerState =
              CellularAutomatonSimulationUtil.stateSizeInBytes(state.length) * 8;
          long cellOutputBitsPerState = CellularAutomatonSimulationUtil.stateSizeInBytes(
              analyzer.getCellOutputStateSize()) * 8;
          int cycleLength = analyzer.getCycleLength();

          int avgStateDiff = analyzer.getAverageStateDifference();
          int avgSetBitCount = analyzer.getAverageSetBitCount();
          synchronized (out) {
            out.printf("%d,%d,%d,%d,%d,%d%n",
                analyzer.getCycleStartIteration(),
                cycleLength > 0 ? cycleLength : maxIterationCount,
                avgStateDiff,
                avgSetBitCount,
                cellOutputBitsPerState,
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

      CellularAutomatonDescriptor descriptor =
          CellularAutomatonDescriptorReader.read(new FileInputStream(inputFilename));

      PrintStream out = System.out;
      if (outputFilename != null) {
        out = new PrintStream(outputFilename);
      }

      out.println("cycleStartIteration,cycleLength,avgDifference,avgSetBitCount,"
          + "cellOutputBitsPerState,bitsPerState");

      ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);
      for(int cnt = 1; cnt <= THREAD_COUNT; cnt++) {
        service.submit(new AnalysisTask(
            sampleSize / THREAD_COUNT, maxIterationCount, descriptor, out));
        Thread.sleep(1000);
      }

      service.shutdown();
      while(!service.awaitTermination(1, TimeUnit.SECONDS)) {}

      out.close();
    } catch (NumberFormatException | ParseException ex) {
      System.out.println(ex.getMessage());
      System.out.println();
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(CellularAutomatonAnalysisCLI.class.getName(), options);
    }
  }

  private static Options buildOptions() {
    Options options = new Options();

    Option inputOption =
        new Option("i", "input", true, "Cellular automaton descriptor XML file name");
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
