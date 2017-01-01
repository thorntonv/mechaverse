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

/**
 * A base class for cellular automata visualization command line applications.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public abstract class CellularAutomatonCLI {

  public static void main(String[] args, CellularAutomatonCLI cli) throws IOException {
    Options options = buildOptions();

    try {
      CommandLineParser parser = new GnuParser();
      CommandLine cmd = parser.parse(options, args);

      int width = Integer.parseInt(cmd.getOptionValue("width", "1920"));
      int height = Integer.parseInt(cmd.getOptionValue("height", "1080"));

      if (cmd.hasOption('v')) {
        int framesPerSecond = Integer.parseInt(cmd.getOptionValue("frameRate", "60"));
        int frameCount = Integer.parseInt(cmd.getOptionValue("frameCount", "-1"));
        cli.createVisualizer(width, height, framesPerSecond, frameCount);
      } else if (cmd.hasOption('f')) {
        int frameCount = Integer.parseInt(cmd.getOptionValue("frameCount", "1800"));
        CellularAutomaton cells = cli.createCellularAutomaton();
        CellularAutomatonRenderer renderer =
            cli.createCellularAutomatonRenderer(cells, width, height);
        writeImages(cells, renderer, 0, frameCount);
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
      CellularAutomaton cells, int width, int height);

  protected abstract CellularAutomatonVisualizer createVisualizer(int width, int height,
      int framesPerSecond, int frameCount) throws IOException;
  
  private static Options buildOptions() {
    Options options = new Options();
    options.addOption(new Option("v", "visualize", false, "Cellular automaton visualization"));
    options.addOption(new Option("a", "analyze", false, "Analyze cellular automaton"));
    options.addOption(new Option("f", "frames", false, "Generate visualization image frames"));
    options.addOption(new Option("c", "frameCount", true, "Number of frames to generate"));
    options.addOption(new Option("w", "width", true, "Image width"));
    options.addOption(new Option("h", "height", true, "Image height"));
    options.addOption(new Option("r", "frameRate", true, "Visualization frame rate (fps)"));
    return options;
  }
}
