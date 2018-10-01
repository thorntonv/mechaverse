package org.mechaverse.manager.client;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.manager.api.MechaverseManagerApi;
import org.mechaverse.manager.api.model.SimulationInfo;
import org.mechaverse.manager.api.model.Task;
import org.mechaverse.manager.client.spring.MechaverseManagerClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MechaverseManagerClientConfig.class, TestConfig.class})
@Ignore
public class MechaverseManagerClientManualTest {

  private static final byte[] TEST_DATA = "test result".getBytes();

  @Autowired
  private MechaverseManagerApi managerApi;

  @Test
  public void test() {
    SimulationInfo simulationInfo = managerApi.createSimulation("itest");
    assertNotNull(simulationInfo);
    assertNotNull(simulationInfo.getSimulationId());

    simulationInfo.getConfig().setMaxInstanceCount(1);
    simulationInfo.getConfig().setTaskIterationCount(1000);
    managerApi.updateSimulationConfig(simulationInfo.getConfig());

    assertNotNull(managerApi.getSimulationInfo(simulationInfo.getSimulationId()));
    try {
      Task task = managerApi.getTask("itest-client");
      managerApi.submitResult(task.getId(), TEST_DATA);

      byte[] state = managerApi.getState(task.getInstanceId(), task.getSimulationId(), 0L);
      assertArrayEquals(TEST_DATA, state);
    } finally {
      managerApi.deleteSimulation(simulationInfo.getSimulationId());
    }
  }
}
