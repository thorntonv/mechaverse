package org.mechaverse.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.mechaverse.simulation.ant.core.entity.ant.CircuitAntBehavior;
import org.mechaverse.simulation.common.Simulation;
import org.mechaverse.simulation.common.circuit.CircuitDataSource;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.util.ArrayUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * A Swing application for visualizing circuit state. A random state is generated and iterations are
 * performed while the circuit state of one of the entities is shown. If {@link #writeImages} is set
 * an image for each state will be saved to a file. The color for each state value is determined by
 * counting the number of set bits across all 32 bit planes. Values with more set bits will be
 * darker. Values that have changed since the previous state will be colored red.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class CircuitVisualizer extends JFrame {

  // TODO(thorntonv): Consider moving this class to mechaverse-client-common.

  private static final long serialVersionUID = 1L;

  public static class CircuitStateRenderer {

    private final int luCount;
    private final int luStateSize;
    private final int scale;
    private int[] circuitState;
    private int[] previousState;

    public CircuitStateRenderer(int luCount, int luStateSize, int scale) {
      this.luCount = luCount;
      this.luStateSize = luStateSize;
      this.scale = scale;
    }

    public void setCircuitState(int[] circuitState, int[] previousState) {
      this.circuitState = circuitState;
      this.previousState = previousState;
    }

    public BufferedImage draw() {
      BufferedImage image = new BufferedImage(
          getPreferredSize().width, getPreferredSize().height, BufferedImage.TYPE_INT_RGB);
      Graphics2D g2d = image.createGraphics();

      for (int x = 0; x < luStateSize; x++) {
        for (int y = 0; y < luCount; y++) {
          int value = circuitState[x * luCount + y];
          int previousValue = previousState[x * luCount + y];

          int setBitCount = getSetBitCount(value);

          int colorValue = 255 - (setBitCount * 7);
          Color color = new Color(colorValue, colorValue, colorValue);
          color = value != previousValue ? new Color(colorValue, 0, 0) : color;

          g2d.setColor(color);
          g2d.fillRect(x * scale, y * scale, scale, scale);
        }
      }
      return image;
    }

    public Dimension getPreferredSize() {
      int width = luStateSize * scale;
      int height = luCount * scale;
      return new Dimension(width + width % 2, height + height % 2);
    }

    private int getSetBitCount(int value) {
      int setBitCount = 0;
      for (int cnt = 1; cnt <= 32; cnt++) {
        if ((value & 0b1) == 1) {
          setBitCount++;
        }
        value >>= 1;
      }
      return setBitCount;
    }
  }

  public static class BufferedImageView extends JPanel {

    // TODO(thorntonv): Move this to a common utility class.

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

  private final CircuitStateRenderer renderer;
  private final BufferedImageView imageView;
  private final Simulation simulation;
  private int[] circuitState;
  private int[] previousState;
  private boolean writeImages = false;

  public static void main(String[] args) throws Exception {
    try (AbstractApplicationContext ctx = getApplicationContext()) {
      final CircuitVisualizer circuitVisualizer =
          new CircuitVisualizer(ctx.getBean(CircuitDataSource.class), createSimulation(ctx));
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          circuitVisualizer.setVisible(true);
        }
      });

      circuitVisualizer.start();
    }
  }

  public CircuitVisualizer(CircuitDataSource circuitDataSource, Simulation simulation)
      throws Exception {
    this.simulation = simulation;
    CircuitSimulationModel circuitModel = circuitDataSource.getCircuitSimulationModel();
    int scaleX = Toolkit.getDefaultToolkit().getScreenSize().width
        / circuitModel.getLogicalUnitInfo().getStateSize();
    int scaleY = Toolkit.getDefaultToolkit().getScreenSize().height
        / circuitModel.getLogicalUnitCount();
    int luCount = circuitModel.getLogicalUnitCount();
    int luStateSize = circuitModel.getLogicalUnitInfo().getStateSize();
    int scale = Math.min(scaleX, scaleY);

    this.renderer = new CircuitStateRenderer(luCount, luStateSize, scale);
    this.imageView = new BufferedImageView();

    this.circuitState = new int[luStateSize * luCount];
    this.previousState = new int[circuitState.length];

    initUI();
  }

  public void start() throws Exception {
    SimulationDataStore state = simulation.generateRandomState();
    simulation.setState(state);

    String circuitStateKey = null;
    int iteration = 0;
    while (true) {
      simulation.step(1);
      state = simulation.getState();

      iteration++;
      if (circuitStateKey == null) {
        for (String key : state.keySet()) {
          if (key.endsWith(CircuitAntBehavior.CIRCUIT_STATE_KEY)) {
            circuitStateKey = key;
          }
        }
      }

      if (circuitStateKey != null) {
        final int[] circuitState = ArrayUtil.toIntArray(state.get(circuitStateKey));
        if (circuitState == null) {
          circuitStateKey = null;
          continue;
        }

        setCircuitState(circuitState);
        renderer.setCircuitState(circuitState, previousState);
        BufferedImage image = renderer.draw();

        imageView.setImage(image);
        imageView.repaint();

        if (writeImages) {
          String imageFilename = String.format("images/iteration-%04d.png", iteration);
          ImageIO.write(image, "PNG", new File(imageFilename));
        }
      }
    }
  }

  private void initUI() {
    setTitle("Mechaverse - Circuit Visualizer");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    add(imageView);
    setSize(renderer.getPreferredSize());
    setLocationRelativeTo(null);
  }

  private void setCircuitState(int[] circuitState) {
    int[] temp = this.previousState;
    this.previousState = this.circuitState;
    this.circuitState = temp;
    System.arraycopy(circuitState, 0, this.circuitState, 0, circuitState.length);
  }

  private static AbstractApplicationContext getApplicationContext() {
    return new ClassPathXmlApplicationContext("simulation-context.xml");
  }

  private static Simulation createSimulation(ApplicationContext ctx) {
    return ctx.getBean(Simulation.class);
  }
}
