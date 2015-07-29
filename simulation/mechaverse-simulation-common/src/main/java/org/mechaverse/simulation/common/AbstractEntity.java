package org.mechaverse.simulation.common;

import java.util.UUID;

import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.SimulatorCellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;

/**
 * An entity base class.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public abstract class AbstractEntity {

  private final String id;
  private GeneticDataStore geneticDataStore;
  private SimulatorCellularAutomaton cellularAutomaton;
  private CellularAutomatonSimulationModel model;

  public AbstractEntity() {
    this(UUID.randomUUID().toString());
  }

  public AbstractEntity(String id) {
    this.id = id;
  }

  public abstract int[] getInput();

  public abstract void processOutput(int[] output);

  public String getId() {
    return id;
  }

  public GeneticDataStore getGeneticDataStore() {
    return geneticDataStore;
  }

  public void setGeneticDataStore(GeneticDataStore geneticDataStore) {
    this.geneticDataStore = geneticDataStore;
  }

  public CellularAutomaton getCellularAutomaton() {
    return cellularAutomaton;
  }

  public void setCellularAutomaton(SimulatorCellularAutomaton cellularAutomaton) {
    this.cellularAutomaton = cellularAutomaton;
  }

  public CellularAutomatonSimulationModel getCellularAutomatonModel() {
    return model;
  }

  public void setCellularAutomatonModel(CellularAutomatonSimulationModel model) {
    this.model = model;
  }
}
