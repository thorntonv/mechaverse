package org.mechaverse.simulation.common.cellautomaton.examples;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.simulation.common.cellautomaton.simulation.AbstractCellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.ThreeNeighborCellConnector;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonRenderer;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonVisualizer;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

/**
 * A cellular automaton where the output of each cell is determined by computing a Boolean function 
 * of the values of its three neighbor cells.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class BooleanFunction3 extends AbstractCellularAutomaton {

  private static class Boolean3Cell extends AbstractCell {

    private int[][] function = new int[3][8];

    public Boolean3Cell() {
      this.outputs = new int[3];
      this.nextOutputs = new int[3];
      
      RandomGenerator random = new Well19937c();
      for (int outputIdx = 0; outputIdx < function.length; outputIdx++) {
        outputs[outputIdx] = random.nextInt(2);
        for (int idx = 0; idx < function[outputIdx].length; idx++) {
          function[outputIdx][idx] = random.nextInt(2);
        }
      }
    }
    
    @Override
    public int getParam(String name) {
      return 0;
    }

    @Override
    public void setParam(String name, int value) {}

    @Override
    public Collection<String> getParamNames() {
      return Collections.emptyList();
    }

    @Override
    public int getOutputParam(String name, int outputIndex) {
      return 0;
    }

    @Override
    public void setOutputParam(String name, int outputIndex, int value) {}

    @Override
    public Collection<String> getOutputParamNames(int outputIndex) {
      return Collections.emptyList();
    }

    @Override
    public void beforeUpdate() {
      int neighborValue = neighbors[0].getOutput(2) & 0b1;
      neighborValue <<= 1;
      neighborValue |= neighbors[1].getOutput(1) & 0b1;
      neighborValue <<= 1;
      neighborValue |= neighbors[2].getOutput(0) & 0b1;

      for (int idx = 0; idx < nextOutputs.length; idx++) {
        nextOutputs[idx] = (function[idx][neighborValue]);
      }
    }

    public void update() {
      int[] tmp = outputs;
      outputs = nextOutputs;
      nextOutputs = tmp;
    }
  }
 
  static final Function<Cell, Color> CELL_COLOR_PROVIDER = new Function<Cell, Color>() {
    @Override
    public Color apply(Cell cell) {
      int colorValue = 0;
      for (int outputIdx = 0; outputIdx < cell.getOutputCount(); outputIdx++) {
        colorValue <<= 1;
        colorValue |= cell.getOutput(outputIdx) & 0b1;
      }
      colorValue = colorValue * 32;
      return new Color(colorValue % 256, colorValue % 256, colorValue % 256);
    }
  };
  
  public BooleanFunction3(int width, int height) {
    super(width, height, new Supplier<Boolean3Cell>() {
      @Override
      public Boolean3Cell get() {
        return new Boolean3Cell();
      }
    }, new ThreeNeighborCellConnector());
  }
  
  public static void main(String[] args) throws IOException {
    CellularAutomatonCLI cli = new CellularAutomatonCLI() {
      @Override
      protected CellularAutomaton createCellularAutomaton() throws IOException {
        return new BooleanFunction3(64, 64);
      }

      @Override
      protected CellularAutomatonRenderer createCellularAutomatonRenderer(CellularAutomaton cells,
          int width, int height) {
        return new CellularAutomatonRenderer(cells, CELL_COLOR_PROVIDER, width, height);
      }

      @Override
      protected CellularAutomatonVisualizer createVisualizer(int width, int height,
          int framesPerSecond, int frameCount) throws IOException {
        return new CellularAutomatonVisualizer(createCellularAutomaton(), CELL_COLOR_PROVIDER,
            width, height, framesPerSecond, frameCount);
      }
    };
    CellularAutomatonCLI.main(args, cli);
  }
}
