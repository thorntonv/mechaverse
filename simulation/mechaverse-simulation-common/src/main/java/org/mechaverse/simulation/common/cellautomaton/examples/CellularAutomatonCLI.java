package org.mechaverse.simulation.common.cellautomaton.examples;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonRenderer;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonVisualizer;

public abstract class CellularAutomatonCLI {

  protected static void main(String[] args, CellularAutomatonCLI cli) throws IOException {
    Options options = buildOptions();

    try {
      CommandLineParser parser = new GnuParser();
      CommandLine cmd = parser.parse(options, args);

      if (cmd.hasOption('v')) {
        cli.createVisualizer().start();
      } else if (cmd.hasOption('f')) {
        int frameCount = Integer.parseInt(cmd.getOptionValue("frameCount", "60"));
        CellularAutomaton cells = cli.createCellularAutomaton();
        writeImages(cells, cli.createCellularAutomatonRenderer(cells), 0, frameCount);
      } else if (cmd.hasOption('a')) {
        // TODO(thorntonv): Integrate analysis.
      } else {
        throw new ParseException("No option specified");
      }     
    } catch (ParseException ex) {
      System.out.println(ex.getMessage());
      System.out.println();
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(cli.getClass().getName(), options);
    }    
  }
  
  public static void writeImages(CellularAutomaton cells, CellularAutomatonRenderer renderer,
      int startIteration, int endIteration) throws IOException {
    System.out.println("Performing simulation ... ");
    for (int iteration = 1; iteration <= endIteration; iteration++) {
      System.out.printf("  Iteration %d / %d%n", iteration, endIteration);
      if (iteration >= startIteration && iteration <= endIteration) {
        BufferedImage image = renderer.draw();
        String imageFilename = String.format("images/iteration-%04d.png", iteration);
        ImageIO.write(image, "PNG", new File(imageFilename));
      }
      cells.update();
    }
  }

  protected abstract CellularAutomaton createCellularAutomaton() throws IOException;

  protected abstract CellularAutomatonRenderer createCellularAutomatonRenderer(
      CellularAutomaton cells);

  protected abstract CellularAutomatonVisualizer createVisualizer() throws IOException;
  
  private static final Options buildOptions() {
    Options options = new Options();
    options.addOption(new Option("v", "visualize", false, "Cellular automaton visualization"));
    options.addOption(new Option("a", "analyze", false, "Analyze cellular automaton"));
    options.addOption(new Option("f", "frames", false, "Generate visualization image frames"));
    options.addOption(new Option("c", "frameCount", true, "Number of frames to generate"));
    return options;
  }
}
