package org.mechaverse.simulation.common.cellautomaton.simulation;

import java.util.Collection;

import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel.CellInfo;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModelBuilder;

import com.google.common.base.Preconditions;

/**
 * Provides a {@link CellularAutomaton} interface to a cellular automaton simulated using
 * {@link CellularAutomatonSimulator}.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class SimulatorCellularAutomaton implements CellularAutomaton {

  private final class SimulatorCellularAutomatonCell implements CellularAutomaton.Cell {

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
      return index * model.getStateSize() + luIndex + stateIndex * model.getLogicalUnitCount();
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
  private final Cell[][] cells;
  private int[] state;

  public SimulatorCellularAutomaton(CellularAutomatonDescriptor descriptor,
      CellularAutomatonSimulator simulator) {
    this(descriptor, simulator, simulator.getAllocator().allocate());
  }

  public SimulatorCellularAutomaton(CellularAutomatonDescriptor descriptor,
      CellularAutomatonSimulator simulator, int index) {
    this.model = CellularAutomatonSimulationModelBuilder.build(descriptor);
    this.simulator = simulator;
    this.index = index;
    this.state = new int[simulator.getAutomatonStateSize()];
    
    // TODO(thorntonv): Handle case where width does not match for all rows.
    int luWidth = descriptor.getLogicalUnit().getRows().get(0).getCells().size();
    int luHeight = descriptor.getLogicalUnit().getRows().size();
    
    cells = new Cell[descriptor.getHeight() * luHeight][descriptor.getWidth() * luWidth];
    for (int luRow = 0; luRow < descriptor.getHeight(); luRow++) {
      for (int luCol = 0; luCol < descriptor.getWidth(); luCol++) {
        int luIndex = luRow * descriptor.getHeight() + luCol;
        for (int row = 0; row < luHeight; row++) {
          for (int col = 0; col < luWidth; col++) {
            CellInfo cellInfo = model.getLogicalUnitInfo().getCells().get(row * luHeight + col);
            cells[luRow * luHeight + row][luCol * luWidth + col] =
                new SimulatorCellularAutomatonCell(cellInfo, luIndex);
          }
        }
      }
    }
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
  public Cell getCell(int row, int column) {
    Preconditions.checkElementIndex(row, cells.length);
    Preconditions.checkElementIndex(column, cells[row].length);
    return cells[row][column];
  }
  
  public void update() {
    simulator.setAutomatonState(index, state);
    simulator.update();
    simulator.getAutomatonState(index, state);
  }
}
