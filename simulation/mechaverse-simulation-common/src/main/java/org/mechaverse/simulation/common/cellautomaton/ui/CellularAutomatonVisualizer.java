package org.mechaverse.simulation.common.cellautomaton.ui;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton.Cell;

import com.google.common.base.Function;

/**
 * A {@link CellularAutomaton} visualizer.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class CellularAutomatonVisualizer extends JFrame {

  private static final long serialVersionUID = 1L;
  private static final String TITLE = "Cellular Automaton Simulator";

  private final CellularAutomaton cellularAutomaton;
  private final CellularAutomatonRenderer renderer;
  private final BufferedImageView imageView;

  public CellularAutomatonVisualizer(CellularAutomaton cellularAutomaton,
      Function<Cell, Color> cellColorProvider) {
    this.cellularAutomaton = cellularAutomaton;
    this.renderer = new CellularAutomatonRenderer(cellularAutomaton, cellColorProvider);
    this.imageView = new BufferedImageView();

    initUI();
    
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        setVisible(true);
      }
    });
  }
  
  public void start() {
    try {
      while (true) {
        update();
        Thread.sleep(500);
      }
    } catch (InterruptedException e) {}
  }
  
  public void update() {
    imageView.setImage(renderer.draw());
    imageView.repaint();
    cellularAutomaton.update();
  }
  
  private void initUI() {
    setTitle(TITLE);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    add(imageView);
    setResizable(false);
    imageView.setPreferredSize(renderer.getPreferredSize());
    pack();
    setLocationRelativeTo(null);
  }
}
