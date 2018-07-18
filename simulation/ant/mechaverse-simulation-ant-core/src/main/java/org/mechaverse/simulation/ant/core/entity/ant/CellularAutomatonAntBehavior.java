package org.mechaverse.simulation.ant.core.entity.ant;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.model.AntSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.CellularAutomatonEntityBehavior;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorDataSource;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;

/**
 * A {@link AbstractAntBehavior} implementation that is based on a simulated cellular automaton.
 */
public class CellularAutomatonAntBehavior extends AbstractAntBehavior {

  public static final String AUTOMATON_STATE_KEY = "cellularAutomatonState";
  public static final String AUTOMATON_OUTPUT_MAP_KEY = "cellularAutomatonOutputMap";
  public static final String AUTOMATON_BIT_OUTPUT_MAP_KEY = "cellularAutomatonBitOutputMap";

  private final AntOutput output = new AntOutput();
  private final CellularAutomatonEntityBehavior cellularAutomatonEntityBehavior;

  public CellularAutomatonAntBehavior(
      CellularAutomatonDescriptorDataSource dataSource, CellularAutomatonSimulator simulator) {
    this.cellularAutomatonEntityBehavior = new CellularAutomatonEntityBehavior(null, AntOutput.DATA_SIZE, dataSource, simulator);
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
