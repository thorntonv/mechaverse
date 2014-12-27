package org.mechaverse.simulation.common.cellautomaton.examples;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonRenderer;

public class CellularAutomatonCLI {


  public static void main(String[] args) {
    
  }
  
  public static void writeImages(CellularAutomaton cells, CellularAutomatonRenderer renderer,
      int startIteration, int endIteration) throws IOException {
    System.out.println("Performing simulation ... ");
    for (int iteration = 1; iteration <= endIteration; iteration++) {
      System.out.printf("\tIteration %d / %d", iteration, endIteration);
      if (iteration >= startIteration && iteration <= endIteration) {
        BufferedImage image = renderer.draw();
        String imageFilename = String.format("images/iteration-%04d.png", iteration);
        ImageIO.write(image, "PNG", new File(imageFilename));
      }
    }

    cells.update();
  }
}
