package org.mechaverse.simulation.common.cellautomaton.ui;

import com.google.common.base.Function;
import org.apache.commons.math3.util.Pair;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton.Cell;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

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
  private final int framesPerSecond;
  private final int frameCount;
  private final AtomicBoolean running = new AtomicBoolean(false);

  @SuppressWarnings("unused")
  public CellularAutomatonVisualizer(CellularAutomaton cellularAutomaton,
      Function<Cell, Color> cellColorProvider, int width, int height, int framesPerSecond) {
    this(cellularAutomaton, cellColorProvider, width, height, framesPerSecond, -1);
  }

  public CellularAutomatonVisualizer(CellularAutomaton cellularAutomaton,
      Function<Cell, Color> cellColorProvider, int width, int height, int framesPerSecond, int frameCount) {
    this.cellularAutomaton = cellularAutomaton;
    this.renderer =
        new CellularAutomatonRenderer(cellularAutomaton, cellColorProvider, width, height);
    this.imageView = new BufferedImageView();
    this.framesPerSecond = framesPerSecond;
    this.frameCount = frameCount;

    initUI();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        setVisible(true);
      }
    });
  }

  public void start() {
    if (running.compareAndSet(false, true)) {
      try {
        int cnt = 1;
        while (frameCount == -1 || cnt <= frameCount) {
          update();
          Thread.sleep(1000 / framesPerSecond);
          cnt++;
        }
      } catch (InterruptedException ignored) {}
      running.set(false);
    }
  }

  public void update() {
    cellularAutomaton.update();
    cellularAutomaton.updateInputs();

    repaintUI();
  }

  public void repaintUI() {
    imageView.setImage(renderer.draw());
    imageView.repaint();
  }

  private void initUI() {
    setTitle(TITLE);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    add(imageView);
    setResizable(false);
    imageView.setPreferredSize(renderer.getPreferredSize());
    pack();
    setLocationRelativeTo(null);
    repaintUI();

    this.addMouseListener(new MouseInputAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        Pair<Integer, Integer> coord = renderer.getCell(e.getX(), e.getY());
        Cell cell = cellularAutomaton.getCell(coord.getFirst(), coord.getSecond());
        cell.setOutput(0, ~cell.getOutput(0));
        imageView.setImage(renderer.draw());
        imageView.repaint();
      }
    });

    this.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
          case KeyEvent.VK_ENTER:
            // Start performing updates.
            new Timer().schedule(new TimerTask() {
              @Override
              public void run() {
                start();
              }
            }, 0);
            break;
          case KeyEvent.VK_SPACE:
            // Perform a single update.
            if (running.compareAndSet(false, true)) {
              update();
              running.set(false);
            }
            break;
        }
      }
    });
  }
}
