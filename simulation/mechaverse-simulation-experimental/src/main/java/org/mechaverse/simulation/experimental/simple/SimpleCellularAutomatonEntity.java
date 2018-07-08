package org.mechaverse.simulation.experimental.simple;

import java.util.UUID;

import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticData;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticData.CellGeneticData;
import org.mechaverse.simulation.common.cellautomaton.simulation.SimulatorCellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;

/**
 * An entity base class.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public abstract class SimpleCellularAutomatonEntity {

  private final String id;
  private GeneticDataStore geneticDataStore;
  private CellularAutomatonGeneticData cellularAutomatonGeneticData;
  private SimulatorCellularAutomaton cellularAutomaton;
  private CellularAutomatonSimulationModel model;

  private static final int[] EMPTY_INPUT = new int[0];

  public SimpleCellularAutomatonEntity() {
    this(UUID.randomUUID().toString());
  }

  public SimpleCellularAutomatonEntity(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public int[] getInput() {
    return EMPTY_INPUT;
  }

  public void processOutput(int[] output) {}

  public GeneticDataStore getGeneticDataStore() {
    return geneticDataStore;
  }

  public void setGeneticDataStore(GeneticDataStore geneticDataStore) {
    this.geneticDataStore = geneticDataStore;
    this.cellularAutomatonGeneticData = new CellularAutomatonGeneticData(
        geneticDataStore.get(SimpleSimulation.AUTOMATON_STATE_KEY), getCellularAutomatonModel());
  }

  public CellularAutomatonGeneticData getCellularAutomatonGeneticData() {
    return cellularAutomatonGeneticData;
  }

  public SimulatorCellularAutomaton getCellularAutomaton() {
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

  protected String cellValueToString(int value) {
    return String.valueOf(value);
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder(getId() + "\n");

    for (int row = 0; row < cellularAutomatonGeneticData.getRowCount(); row++) {
      for (int col = 0; col < cellularAutomatonGeneticData.getColumnCount(); col++) {
        CellGeneticData cellData = cellularAutomatonGeneticData.getCellData(row, col);
        str.append(cellValueToString(cellData.getData()[0])).append("\t");
      }
      str.append("\n");
    }
    return str.toString();
  }
}
