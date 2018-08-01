package org.mechaverse.simulation.ant.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.ant.core.spring.SimpleAntSimulationConfig;
import org.mechaverse.simulation.ant.core.ui.AntSimulationImageProvider;
import org.mechaverse.simulation.ant.core.ui.AntSimulationVisualizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Stopwatch;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleAntSimulationConfig.class})
public class AntSimulationVisualizerTest {

    private static final int RUN_TIME_SECONDS = 4;
    private static final int FRAMES_PER_SECOND = 30;

    @Autowired
    private AntSimulationImpl simulation;

    private AntSimulationVisualizer visualizer;

    @Before
    public void setUp() throws IOException {
        simulation.setState(simulation.generateRandomState());
        visualizer = new AntSimulationVisualizer(
                simulation, AntSimulationImageProvider.DEFAULT_CELL_SIZE / 2,
                FRAMES_PER_SECOND, RUN_TIME_SECONDS * FRAMES_PER_SECOND
        );
    }

    @Test
    public void testUpdate() throws IOException {
        byte[] stateData = simulation.getStateData();
        visualizer.update();
        assertNotEquals(Arrays.hashCode(stateData), Arrays.hashCode(simulation.getStateData()));
    }

    @Test
    public void testStart() throws IOException, InterruptedException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        byte[] stateData = simulation.getStateData();
        visualizer.start();
        assertTrue(stopwatch.elapsed(TimeUnit.SECONDS) < RUN_TIME_SECONDS + 1);
        assertNotEquals(Arrays.hashCode(stateData), Arrays.hashCode(simulation.getStateData()));
    }
}