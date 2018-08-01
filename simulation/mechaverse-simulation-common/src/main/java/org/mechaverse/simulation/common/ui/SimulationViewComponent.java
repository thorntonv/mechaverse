package org.mechaverse.simulation.common.ui;

import java.awt.*;
import javax.swing.*;

import org.mechaverse.simulation.common.Simulation;
import org.mechaverse.simulation.common.model.SimulationModel;

public class SimulationViewComponent extends JPanel implements Scrollable {

    private final Simulation simulation;
    private final SimulationRenderer renderer;

    public SimulationViewComponent(final Simulation simulation, final SimulationRenderer renderer) {
        this.simulation = simulation;
        this.renderer = renderer;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
        return 50;
    }

    @Override
    public int getScrollableBlockIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        SimulationModel state = simulation.getState();
        renderer.draw((Graphics2D) g.create(), state, state.getEnvironment());
    }

    @Override
    public Dimension getPreferredSize() {
        return renderer.getPreferredSize(simulation.getState().getEnvironment());
    }
}
