package org.mechaverse.simulation.primordial.core;

import java.io.IOException;
import org.mechaverse.simulation.common.AbstractSimulation;
import org.mechaverse.simulation.common.EnvironmentFactory;
import org.mechaverse.simulation.common.SimulationModelGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;
import org.mechaverse.simulation.primordial.core.util.PrimordialSimulationModelUtil;

public class PrimordialSimulationImpl extends
    AbstractSimulation<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> implements AutoCloseable {

  public PrimordialSimulationImpl(
      final SimulationModelGenerator<PrimordialSimulationModel> simulationModelGenerator,
      final EnvironmentFactory<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> environmentFactory) {
    super(simulationModelGenerator, environmentFactory);
  }

  @Override
  public void setState(PrimordialSimulationModel model) {
    model.getEnvironments().forEach(PrimordialEnvironmentModel::initCells);
    super.setState(model);
  }

  @Override
  public void setStateData(byte[] data) throws Exception {
    setState(PrimordialSimulationModelUtil.deserialize(data));
  }

  @Override
  public PrimordialSimulationModel deserializeState(byte[] stateData) throws IOException {
    return PrimordialSimulationModelUtil.deserialize(stateData);
  }

  @Override
  public byte[] getStateData() throws IOException {
    return PrimordialSimulationModelUtil.serialize(getState());
  }
}
