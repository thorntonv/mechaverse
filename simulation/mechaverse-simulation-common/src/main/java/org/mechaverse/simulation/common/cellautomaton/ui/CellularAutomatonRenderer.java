package org.mechaverse.simulation.common.cellautomaton.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton.Cell;

import com.google.common.base.Function;

/**
 * Draws a {@link CellularAutomaton} to a {@link BufferedImage}.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class CellularAutomatonRenderer {

  private CellularAutomaton cells;
  private int scale;
  private Function<Cell, Color> cellColorProvider;

  public CellularAutomatonRenderer(
      CellularAutomaton cells, Function<Cell, Color> cellColorProvider) {
    this(cells, cellColorProvider, 
      3 * Toolkit.getDefaultToolkit().getScreenSize().width / 4,
      3 * Toolkit.getDefaultToolkit().getScreenSize().height / 4);
  }

  public CellularAutomatonRenderer(CellularAutomaton cells, Function<Cell, Color> cellColorProvider, 
      int maxWidth, int maxHeight) {
    this(cells, Math.min(maxWidth / cells.getWidth(), maxHeight / cells.getHeight()));
    this.cellColorProvider = cellColorProvider;
  }
  
  public CellularAutomatonRenderer(CellularAutomaton cells, int scale) {
    this.cells = cells;
    this.scale = Math.max(scale, 1);
  }
  
  public BufferedImage draw() {
    BufferedImage image = new BufferedImage(
        getPreferredSize().width, getPreferredSize().height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = image.createGraphics();
    g2d.setColor(Color.DARK_GRAY);
    g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
    for (int row = 0; row < cells.getHeight(); row++) {
      for (int col = 0; col < cells.getWidth(); col++) {
        Cell cell = cells.getCell(row, col);
        g2d.setColor(cellColorProvider.apply(cell));
        if (scale > 2) {
          g2d.fillRect(col * scale + 1, row * scale + 1, scale - 2, scale - 2);
        } else {
          g2d.fillRect(col * scale, row * scale, scale, scale);
        }
      }
    }
    return image;
  }

  public Dimension getPreferredSize() {
    return new Dimension(cells.getWidth() * scale, cells.getHeight() * scale);
  }
}