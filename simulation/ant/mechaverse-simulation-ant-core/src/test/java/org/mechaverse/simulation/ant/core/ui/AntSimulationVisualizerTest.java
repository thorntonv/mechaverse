package org.mechaverse.simulation.ant.core.ui;

import static org.junit.Assert.assertNotEquals;

import com.google.common.base.Stopwatch;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.ant.core.AntSimulationImpl;
import org.mechaverse.simulation.ant.core.spring.AntSimulationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AntSimulationConfig.class})
public class AntSimulationVisualizerTest {

  private static final int RUN_TIME_SECONDS = 5;
  private static final int FRAMES_PER_SECOND = 15;

  private static final Logger logger = LoggerFactory.getLogger(AntSimulationVisualizerTest.class);

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

  @After
  public void tearDown() {
    visualizer.dispose();
    visualizer = null;
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