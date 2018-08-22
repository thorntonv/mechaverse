package org.mechaverse.simulation.primordial.core.ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.random.Well19937c;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationImpl;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationModelGenerator;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;
import org.mechaverse.simulation.primordial.core.spring.PrimordialSimulationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.google.common.base.Stopwatch;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PrimordialSimulationConfig.class})
public class PrimordialSimulationVisualizerTest {

  private static final int RUN_TIME_SECONDS = 5;
  private static final int FRAMES_PER_SECOND = 15;

  private static final Logger logger = LoggerFactory.getLogger(PrimordialSimulationVisualizerTest.class);

  @Autowired
  private PrimordialSimulationImpl simulation;

  private PrimordialSimulationVisualizer visualizer;

  @Before
  public void setUp() {
    PrimordialSimulationModelGenerator modelGenerator =
        new PrimordialSimulationModelGenerator(0);
    PrimordialSimulationModel model = modelGenerator.generate(new Well19937c());

    simulation.setState(model);
    visualizer = new PrimordialSimulationVisualizer(
        simulation, PrimordialSimulationImageProvider.DEFAULT_CELL_SIZE,
        FRAMES_PER_SECOND, RUN_TIME_SECONDS * FRAMES_PER_SECOND
    );
  }

  @After
  public void tearDown() {
    visualizer.dispose();
    visualizer = null;
    simulation.close();
  }

  @Test
  public void testUpdate() throws IOException {
    byte[] stateData = simulation.getStateData();
    visualizer.update();
    assertNotEquals(Arrays.hashCode(stateData), Arrays.hashCode(simulation.getStateData()));
  }

  @Test
  public void testStart() throws IOException {
    Stopwatch stopwatch = Stopwatch.createStarted();
    byte[] stateData = simulation.getStateData();
    visualizer.start();
    stopwatch.stop();
    logger.info((float) (RUN_TIME_SECONDS * FRAMES_PER_SECOND) / stopwatch.elapsed(TimeUnit.SECONDS) + " fps.");

    assertNotEquals(Arrays.hashCode(stateData), Arrays.hashCode(simulation.getStateData()));
  }
}