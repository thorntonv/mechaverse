package org.mechaverse.simulation.primordial.core.entity;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.cellautomaton.CellularAutomatonEntityBehavior;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorDataSource;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialEntityModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;

/**
 * A {@link AbstractPrimordialEntityBehavior} implementation that is based on a simulated cellular automaton.
 */
public class CellularAutomatonPrimordialEntityBehavior extends AbstractPrimordialEntityBehavior {

  private CellularAutomatonEntityBehavior<
          PrimordialSimulationModel,
          PrimordialEnvironmentModel,
          EntityModel<EntityType>,
          EntityType> cellAutomatonBehavior;

  private PrimordialEntityOutput output = new PrimordialEntityOutput();

  public CellularAutomatonPrimordialEntityBehavior(
      PrimordialEntityModel entityModel,
      CellularAutomatonDescriptorDataSource dataSource, CellularAutomatonSimulator simulator) {
    super(entityModel);
  }

  @Override
  public void setInput(PrimordialEntityInput input, RandomGenerator random) {
    cellAutomatonBehavior.setInput(input.getData(), random);
  }

  @Override
  public PrimordialEntityOutput getOutput(RandomGenerator random) {
    int[] outputData = cellAutomatonBehavior.getOutput(random);
    output.setData(outputData);
    return output;
  }

  @Override
  public void onRemoveEntity() {
    cellAutomatonBehavior.onRemoveEntity();
  }

  @Override
  public void setState(PrimordialSimulationModel state) {
    cellAutomatonBehavior.setState(state);
  }

  @Override
  public void updateState(PrimordialSimulationModel state) {
    cellAutomatonBehavior.updateState(state);
  }
}
