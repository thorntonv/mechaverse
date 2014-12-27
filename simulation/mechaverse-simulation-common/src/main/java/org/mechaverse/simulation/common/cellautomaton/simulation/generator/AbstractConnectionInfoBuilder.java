package org.mechaverse.simulation.common.cellautomaton.simulation.generator;

import java.util.List;
import java.util.Map;

import org.mechaverse.cellautomaton.model.Cell;
import org.mechaverse.cellautomaton.model.CellType;
import org.mechaverse.cellautomaton.model.Output;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.ConnectionInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.Input;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.ExternalCell.ExternalCellType;

/**
 * Connects cells in the matrix.
 */
public abstract class AbstractConnectionInfoBuilder implements ConnectionInfoBuilder {

  protected final Cell[][] cells;
  protected final Map<String, CellType> cellTypeMap;

  public AbstractConnectionInfoBuilder(Cell[][] cells, Map<String, CellType> cellTypeMap) {
    this.cells = cells;
    this.cellTypeMap = cellTypeMap;
  }

  @Override
  public abstract ConnectionInfo build();

  protected Input createExternalInput(int relativeUnitRow, int relativeUnitCol,
      Cell cell, int outputIdx, List<ExternalCell> externalCells) {
    ExternalCell externalCell =
        createExternalCell(relativeUnitRow, relativeUnitCol, cell, outputIdx, externalCells);
    Input input = new Input(externalCell, ExternalCellType.INSTANCE.getOutput());
    return input;
  }

  protected ExternalCell createExternalCell(int relativeUnitRow, int relativeUnitCol,
      Cell cell, int outputIdx, List<ExternalCell> externalCells) {
    int id = externalCells.size() + 1;
    CellType cellType = cellTypeMap.get(cell.getType());
    Output output = cellType.getOutputs().get(outputIdx % cellType.getOutputs().size());
    ExternalCell externalCell =
        new ExternalCell(relativeUnitRow, relativeUnitCol, cell.getId(), output.getId());
    externalCell.setId(CellularAutomatonSimulationModel.EXTERNAL_INPUT_ID_PREFIX + String.valueOf(id));
    externalCells.add(externalCell);
    return externalCell;
  }

  protected Input createCellInput(Cell cell, int outputIdx) {
    CellType cellType = cellTypeMap.get(cell.getType());
    return new Input(cell, cellType.getOutputs().get(
        outputIdx % cellType.getOutputs().size()));
  }
}