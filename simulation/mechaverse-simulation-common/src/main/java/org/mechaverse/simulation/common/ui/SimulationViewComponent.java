package org.mechaverse.simulation.common.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import org.mechaverse.simulation.common.Simulation;
import org.mechaverse.simulation.common.model.EnvironmentModel;

public class SimulationViewComponent extends JPanel implements Scrollable, ActionListener {

  private final Simulation simulation;
  private final SimulationRenderer renderer;
  private Map<String, BufferedImage> stateImages = new HashMap<>();
  private JComboBox<String> environments;

  public SimulationViewComponent(final Simulation simulation, final SimulationRenderer renderer) {
    this.simulation = simulation;
    this.renderer = renderer;

    List<String> environmentIds = new ArrayList<>();
    for (Object envModel : simulation.getState().getEnvironments()) {
      if (envModel instanceof EnvironmentModel) {
        environmentIds.add(((EnvironmentModel) envModel).getId());
      }
    }

    environments = new JComboBox<>(environmentIds.toArray(new String[]{}));
    environments.setSelectedIndex(0);
    environments.addActionListener(this);
    add(environments);
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

  public void setImage(final Map<String, BufferedImage> stateImages) {
    this.stateImages = stateImages;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    String selectedEnvironmentId = (String) environments.getSelectedItem();
    BufferedImage image = stateImages.get(selectedEnvironmentId);
    if(image != null) {
      g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
    }
  }

  @Override
  public Dimension getPreferredSize() {
    return renderer.getPreferredSize(simulation.getState().getEnvironment());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
  }
}
