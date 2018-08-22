package org.mechaverse.simulation.benchmark;

import com.google.common.io.Files;
import java.io.File;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationImpl;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationModelGenerator;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestSimulation {

  private static final String STATE_FILENAME = "/home/thorntonv/Desktop/state.dat";

  public static void main(String[] args) throws Exception {
    //Thread.sleep(20*1000);
    try (ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
        "primordial-simulation-context.xml");
        PrimordialSimulationImpl simulation = appContext.getBean(PrimordialSimulationImpl.class)) {
      PrimordialSimulationModelGenerator modelGenerator =
          new PrimordialSimulationModelGenerator(7);
      if (!new File(STATE_FILENAME).exists()) {
        PrimordialSimulationModel model = modelGenerator.generate(new Well19937c());
        model.setEntityMaxCountPerEnvironment(262144 / 8);
        simulation.setState(model);
      } else {
        byte[] stateData = Files.toByteArray(new File(STATE_FILENAME));
        System.out.println("Loaded " + stateData.length + " bytes from " + STATE_FILENAME);
        simulation.setStateData(stateData);
      }

//      PrimordialSimulationVisualizer visualizer = new PrimordialSimulationVisualizer(simulation, 6, 30, -1);
//      visualizer.start();

      for (int cnt = 1; cnt <= 100; cnt++) {
        simulation.step(25);
        Thread.sleep(50);
      }

//      while (true) {
//
////        byte[] stateData = simulation.getStateData();
////        Files.write(stateData, new File(STATE_FILENAME));
//      }
    }
  }
}
