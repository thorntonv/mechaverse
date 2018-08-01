package org.mechaverse.simulation.common.ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;

import org.mechaverse.simulation.common.Simulation;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;

/**
 * A swing application for visualizing a simulation.
 */
@SuppressWarnings("unused")
public class SimulationVisualizer<SIM_MODEL extends SimulationModel<ENV_MODEL, ENT_MODEL, ENT_TYPE>,
        ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
        ENT_MODEL extends EntityModel<ENT_TYPE>,
        ENT_TYPE extends Enum<ENT_TYPE>> extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String TITLE = "Simulation";

    private final Simulation<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> simulation;
    private final JPanel panel = new JPanel();
    private final JScrollPane scrollPane = new JScrollPane(panel);
    private final SimulationViewComponent simulationComponent;

    private final int framesPerSecond;
    private final int frameCount;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public SimulationVisualizer(Simulation<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> simulation,
            SimulationRenderer<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> renderer,
            int framesPerSecond) {
        this(simulation, renderer, framesPerSecond, -1);
    }

    public SimulationVisualizer(Simulation<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> simulation,
            SimulationRenderer<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> renderer,
            int framesPerSecond, int frameCount) {
        this.simulation = simulation;
        this.framesPerSecond = framesPerSecond;
        this.frameCount = frameCount;
        this.simulationComponent = new SimulationViewComponent(simulation, renderer);

        initUI();

        SwingUtilities.invokeLater(() -> setVisible(true));
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            try {
                int cnt = 1;
                while (frameCount == -1 || cnt <= frameCount) {
                    long startTime = System.currentTimeMillis();
                    update();
                    long endTime = System.currentTimeMillis();
                    long millisToSleep = 1000 / framesPerSecond - (endTime - startTime);
                    if (millisToSleep > 0) {
                        Thread.sleep(millisToSleep);
                    }
                    cnt++;
                }
            } catch (InterruptedException ignored) {
            }
            running.set(false);
        }
    }

    public void stop() {
        running.set(false);
    }

    public void update() {
        try {
            SwingUtilities.invokeAndWait(() -> simulation.step(1));
        } catch (Exception e) {
        }
        SIM_MODEL state = simulation.getState();
        setTitle("Simulation " + state.getId() + " Iteration #" + state.getIteration());
        repaintUI();
    }

    public void repaintUI() {
        simulationComponent.repaint();
    }

    private void initUI() {
        setTitle(TITLE);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        panel.add(simulationComponent);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        getContentPane().add(scrollPane);

        setResizable(true);
        pack();
        setLocationRelativeTo(null);
        repaintUI();

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
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
