package org.mechaverse.simulation.common.cellautomaton.simulation;

import java.util.Collection;

import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.CellInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModelBuilder;

import com.google.common.base.Preconditions;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * Provides a {@link CellularAutomaton} interface to a cellular automaton simulated using
 * {@link CellularAutomatonSimulator}.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class SimulatorCellularAutomaton implements CellularAutomaton {

  public final class SimulatorCellularAutomatonCell implements CellularAutomaton.Cell {

    private CellInfo cellInfo;
    private int luIndex;

    public SimulatorCellularAutomatonCell(CellInfo cellInfo, int luIndex) {
      this.cellInfo = cellInfo;
      this.luIndex = luIndex;
    }

    @Override
    public int getOutput(int idx) {
      String varName = cellInfo.getOutputVarName(cellInfo.getOutputs().get(idx));
      return state[getStateIndex(varName)];
    }

    @Override
    public void setOutput(int idx, int value) {
      String varName = cellInfo.getOutputVarName(cellInfo.getOutputs().get(idx));
      state[getStateIndex(varName)] = value;
    }

    public void addOutputToInputMap(int idx) {
      String varName = cellInfo.getOutputVarName(cellInfo.getOutputs().get(idx));
      int stateIdx = getStateIndex(varName);
      if (!inputMap.contains(stateIdx)) {
        inputMap.add(stateIdx);
      }
    }

    public void removeOutputFromInputMap(int idx) {
      String varName = cellInfo.getOutputVarName(cellInfo.getOutputs().get(idx));
      inputMap.remove(getStateIndex(varName));
    }

    public void addOutputToOutputMap(int idx) {
      String varName = cellInfo.getOutputVarName(cellInfo.getOutputs().get(idx));
      int stateIdx = getStateIndex(varName);
      if (!outputMap.contains(stateIdx)) {
        outputMap.add(stateIdx);
      }
    }

    public void removeOutputFromOutputMap(int idx) {
      String varName = cellInfo.getOutputVarName(cellInfo.getOutputs().get(idx));
      outputMap.remove(getStateIndex(varName));
    }

    @Override
    public int getParam(String paramId) {
      String varName = cellInfo.getParamVarName(paramId);
      return state[getStateIndex(varName)];
    }

    @Override
    public void setParam(String paramId, int value) {
      String varName = cellInfo.getParamVarName(paramId);
      state[getStateIndex(varName)] = value;
    }

    @Override
    public int getOutputParam(String name, int outputIndex) {
      String varName = cellInfo.getOutputParamVarName(cellInfo.getOutputs().get(outputIndex), name);
      return state[getStateIndex(varName)];
    }

    @Override
    public void setOutputParam(String name, int outputIndex, int value) {
      String varName = cellInfo.getOutputParamVarName(cellInfo.getOutputs().get(outputIndex), name);
      state[getStateIndex(varName)] = value;
    }

    private int getStateIndex(String varName) {
      int stateIndex = model.getLogicalUnitInfo().getStateIndex(varName);
      return luIndex + stateIndex * model.getLogicalUnitCount();
    }

    @Override
    public int getOutputCount() {
      return cellInfo.getOutputs().size();
    }

    @Override
    public Collection<String> getParamNames() {
      return cellInfo.getParamVarNames();
    }

    @Override
    public Collection<String> getOutputParamNames(int outputIndex) {
      return cellInfo.getOutputParamVarNames(cellInfo.getOutputs().get(outputIndex));
    }
  }

  private final CellularAutomatonSimulationModel model;
  private final int index;
  private final CellularAutomatonSimulator simulator;
  private final SimulatorCellularAutomatonCell[][] cells;
  private int[] state;
  private final int[] input;
  private final TIntList inputMap = new TIntArrayList();
  private final int[] output;
  private final TIntList outputMap = new TIntArrayList();

  public SimulatorCellularAutomaton(CellularAutomatonDescriptor descriptor,
      CellularAutomatonSimulator simulator) {
    this(descriptor, simulator, simulator.getAllocator().allocate());
  }

  public SimulatorCellularAutomaton(CellularAutomatonDescriptor descriptor,
      CellularAutomatonSimulator simulator, int index) {
    this(CellularAutomatonSimulationModelBuilder.build(descriptor), simulator, index);
  }

  public SimulatorCellularAutomaton(CellularAutomatonSimulationModel model,
      CellularAutomatonSimulator simulator, int index) {
    this.model = model;
    this.simulator = simulator;
    this.index = index;
    this.state = new int[simulator.getAutomatonStateSize()];
    this.input = new int[simulator.getAutomatonInputSize()];
    this.output = new int[simulator.getAutomatonOutputSize()];

    // TODO(thorntonv): Handle case where width does not match for all rows.
    int luWidth = model.getLogicalUnitInfo().getWidth();
    int luHeight = model.getLogicalUnitInfo().getHeight();

    cells = new SimulatorCellularAutomatonCell[model.getHeight() * luHeight][model.getWidth() * luWidth];
    for (int luRow = 0; luRow < model.getHeight(); luRow++) {
      for (int luCol = 0; luCol < model.getWidth(); luCol++) {
        int luIndex = luRow * model.getWidth() + luCol;
        for (int row = 0; row < luHeight; row++) {
          for (int col = 0; col < luWidth; col++) {
            int cellIdx = row * luWidth + col;
            CellInfo cellInfo = model.getLogicalUnitInfo().getCells().get(cellIdx);
            cells[luRow * luHeight + row][luCol * luWidth + col] =
                new SimulatorCellularAutomatonCell(cellInfo, luIndex);
          }
        }
      }
    }
  }

  public CellularAutomatonSimulator getSimulator() {
    return simulator;
  }

  public int[] getState() {
    return state;
  }

  public void setState(int[] state) {
    this.state = state;
  }

  @Override
  public int getWidth() {
    return cells[0].length;
  }

  @Override
  public int getHeight() {
    return cells.length;
  }

  @Override
  public SimulatorCellularAutomatonCell getCell(int row, int column) {
    Preconditions.checkElementIndex(row, cells.length);
    Preconditions.checkElementIndex(column, cells[row].length);
    return cells[row][column];
  }

  public void updateInputs() {
    for (int idx = 0; idx < inputMap.size(); idx++) {
      input[idx] = state[inputMap.get(idx)];
    }
    simulator.setAutomatonInput(index, input);
  }

  public void updateInputMap() {
    simulator.setAutomatonInputMap(index, inputMap.toArray());
  }

  public void refresh() {
    simulator.getAutomatonState(index, state);
  }

  public void refreshOutputs() {
    simulator.getAutomatonOutput(index, output);
    for (int idx = 0; idx < outputMap.size(); idx++) {
      state[outputMap.get(idx)] = output[idx];
    }
  }

  public void updateState() {
    simulator.setAutomatonState(index, state);
  }

  public void updateOutputMap() {
    simulator.setAutomatonOutputMap(index, outputMap.toArray());
  }

  @Override
  public void update() {
    simulator.setAutomatonState(index, state);
    simulator.update();
    simulator.getAutomatonState(index, state);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int row = 0; row < cells.length; row++) {
      for (int col = 0; col < cells[row].length; col++) {
        builder.append(cells[row][col].getOutput(0)).append("\t");
      }
      builder.append("\n");
    }
    return builder.toString();
  }
}
