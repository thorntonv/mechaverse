package org.mechaverse.simulation.bool.majority;

import java.io.File;
import java.io.PrintWriter;

import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.FitnessFunctions;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorReader;
import org.mechaverse.simulation.common.simple.SimpleSimulation;
import org.mechaverse.simulation.common.simple.SimpleSimulationLogger;
import org.mechaverse.simulation.common.simple.SimpleSimulationModel;
import org.mechaverse.simulation.common.simple.SimpleSimulationState;

import com.google.common.base.Supplier;

/**
 * A {@link SimpleSimulation} implementation of the bitwise majority function. Every iteration each
 * entity is provided with a random N bit number as input. Entities that output the bit value that
 * occurred more frequently on the input are assigned a high fitness.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class MajorityFunctionSimulation
    extends SimpleSimulation<MajorityFunctionEntity, SimpleSimulationModel> {

  private static final int NUM_ENTITIES = 2000;
  private static final int NUM_ITERATIONS = 10000;
  
  private static class MajorityFunctionEntitySupplier implements Supplier<MajorityFunctionEntity> {
    
    @Override
    public MajorityFunctionEntity get() {
      return new MajorityFunctionEntity();
    }
  };
  
  public MajorityFunctionSimulation(int populationSize, CellularAutomatonDescriptor descriptor, 
      SimpleSimulationLogger<MajorityFunctionEntity, SimpleSimulationModel> simulationLogger) {
    super(new SimpleSimulationState<>(new SimpleSimulationModel(), 
      SimpleSimulationModel.SERIALIZER), 
      new MajorityFunctionEntitySupplier(),
      MajorityFunctionFitnessCalculator.INSTANCE, 
      populationSize, 
      MajorityFunctionEntity.ENTITY_INPUT_SIZE, 
      MajorityFunctionEntity.ENTITY_OUTPUT_SIZE, 
      descriptor, 
      simulationLogger);
  }
  
  public static void main(String[] args) throws Exception {
    CellularAutomatonDescriptor descriptor = CellularAutomatonDescriptorReader.read(
        ClassLoader.getSystemResourceAsStream("boolean4-small.xml"));
  
    PrintWriter results = new PrintWriter(new File("results.csv"));
    SimpleSimulationLogger<MajorityFunctionEntity, SimpleSimulationModel> simulationLogger =
        new SimpleSimulationLogger<>(results, MajorityFunctionFitnessCalculator.INSTANCE);

    MajorityFunctionSimulation simulation =
        new MajorityFunctionSimulation(NUM_ENTITIES, descriptor, simulationLogger);

    for (int iteration = 1; iteration <= NUM_ITERATIONS; iteration++) {
      simulation.step();
    }
    
    results.close();
  }
}
