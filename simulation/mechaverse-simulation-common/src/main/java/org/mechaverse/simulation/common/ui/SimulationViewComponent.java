package org.mechaverse.simulation.common.ui;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import org.mechaverse.simulation.common.Simulation;

public class SimulationViewComponent extends JPanel implements Scrollable {

  private final Simulation simulation;
  private final SimulationRenderer renderer;
  private BufferedImage image = new BufferedImage(1, 1, TYPE_INT_RGB);

  public SimulationViewComponent(final Simulation simulation, final SimulationRenderer renderer) {
    this.simulation = simulation;
    this.renderer = renderer;
  }

  @Override
  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  @Override
  public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation,
      final int direction) {
    return 50;
  }

  @Override
  public int getScrollableBlockIncrement(final Rectangle visibleRect, final int orientation,
      final int direction) {
    return 50;
  }

  @Override
  public boolean getScrollableTracksViewportWidth() {
    return true;
  }

  @Override
  public boolean getScrollableTracksViewportHeight() {
    return true;
  }

  public void setImage(final BufferedImage image) {
    this.image = image;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
  }

  @Override
  public Dimension getPreferredSize() {
    return renderer.getPreferredSize(simulation.getState().getEnvironment());
  }
}
