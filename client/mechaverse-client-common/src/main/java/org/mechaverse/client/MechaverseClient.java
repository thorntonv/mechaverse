package org.mechaverse.client;

import org.mechaverse.simulation.api.SimulationService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MechaverseClient {

  public final MechaverseClientConfig config;

  public static void main(String[] args) {
    try (ClassPathXmlApplicationContext context =
        new ClassPathXmlApplicationContext("spring/applicationContext.xml")) {
      MechaverseClientConfig config = context.getBean(MechaverseClientConfig.class);

      new MechaverseClient(config).start();
    }
  }

  public MechaverseClient(MechaverseClientConfig config) {
    this.config = config;
  }

  public void start() {
    try {
      SimulationService simulationService = config.getSimulationService();
      System.out.println(simulationService.getInstanceCount() + " instances");
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
}
