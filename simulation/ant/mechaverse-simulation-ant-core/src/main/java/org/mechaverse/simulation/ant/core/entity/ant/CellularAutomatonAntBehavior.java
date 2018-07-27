package org.mechaverse.simulation.ant.core.entity.ant;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.ant.core.model.CellEnvironment;
import org.mechaverse.simulation.ant.core.model.EntityType;
import org.mechaverse.simulation.common.cellautomaton.CellularAutomatonEntityBehavior;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorDataSource;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.model.EntityModel;

/**
 * A {@link AbstractAntBehavior} implementation that is based on a simulated cellular automaton.
 */
@SuppressWarnings("WeakerAccess")
public class CellularAutomatonAntBehavior extends AbstractAntBehavior {

  public static final String AUTOMATON_STATE_KEY = "cellularAutomatonState";
  public static final String AUTOMATON_OUTPUT_MAP_KEY = "cellularAutomatonOutputMap";
  public static final String AUTOMATON_BIT_OUTPUT_MAP_KEY = "cellularAutomatonBitOutputMap";

  private final AntOutput output = new AntOutput();
  private final CellularAutomatonEntityBehavior<AntSimulationModel, CellEnvironment, EntityModel<EntityType>, EntityType> cellularAutomatonEntityBehavior;

  public CellularAutomatonAntBehavior(
      CellularAutomatonDescriptorDataSource dataSource, CellularAutomatonSimulator simulator) {
    this.cellularAutomatonEntityBehavior = new CellularAutomatonEntityBehavior<>(AntOutput.DATA_SIZE, dataSource, simulator);
  }

  @Override
  void setModel(final Ant entity) {
    super.setModel(entity);
    cellularAutomatonEntityBehavior.setEntity(entity);
  }

  @Override
  public void setInput(AntInput input, RandomGenerator random) {
    cellularAutomatonEntityBehavior.setInput(input.getData(), random);
  }

  @Override
  public AntOutput getOutput(RandomGenerator random) {
    output.setData(cellularAutomatonEntityBehavior.getOutput(random));
    return output;
  }

  @Override
  public void onRemoveEntity() {
    cellularAutomatonEntityBehavior.onRemoveEntity();
  }

  @Override
  public void setState(AntSimulationModel state) {
    cellularAutomatonEntityBehavior.setState(state);
  }

  @Override
  public void updateState(AntSimulationModel state) {
    cellularAutomatonEntityBehavior.updateState(state);
  }
}
