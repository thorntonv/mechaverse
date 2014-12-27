package org.mechaverse.simulation.common.cellautomaton.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class BufferedImageView extends JPanel {

  private static final long serialVersionUID = 1L;

  private BufferedImage image;

  public BufferedImageView() {}

  public void setImage(BufferedImage image) {
    this.image = image;
  }

  private void doDrawing(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    g2d.drawImage(image, null, 0, 0);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    doDrawing(g);
  }
}