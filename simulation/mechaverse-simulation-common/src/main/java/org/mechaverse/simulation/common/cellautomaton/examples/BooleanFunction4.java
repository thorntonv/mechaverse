package org.mechaverse.simulation.common.cellautomaton.examples;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.simulation.common.cellautomaton.simulation.AbstractCellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.FourNeighborCellConnector;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonRenderer;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonVisualizer;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

/**
 * A cellular automaton where the output of each cell is determined by computing a Boolean function 
 * of the values of its four neighbor cells.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class BooleanFunction4 extends AbstractCellularAutomaton {

  private static class Boolean4Cell extends AbstractCell {

    private int[][] function = new int[4][16];

    public Boolean4Cell() {
      this.outputs = new int[4];
      this.nextOutputs = new int[4];
      
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
      neighborValue |= neighbors[1].getOutput(3) & 0b1;
      neighborValue <<= 1;
      neighborValue |= neighbors[2].getOutput(0) & 0b1;
      neighborValue <<= 1;
      neighborValue |= neighbors[3].getOutput(1) & 0b1;

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
 
  private static final Function<Cell, Color> CELL_COLOR_PROVIDER = new Function<Cell, Color>() {
    @Override
    public Color apply(Cell cell) {
      int colorValue = 0;
      for (int outputIdx = 0; outputIdx < cell.getOutputCount(); outputIdx++) {
        colorValue <<= 1;
        colorValue |= cell.getOutput(outputIdx) & 0b1;
      }
      colorValue = colorValue * 16;
      return new Color(colorValue % 256, colorValue % 256, colorValue % 256);
    }
  };
  
  public BooleanFunction4(int width, int height) {
    super(width, height, new Supplier<Boolean4Cell>() {
      @Override
      public Boolean4Cell get() {
        return new Boolean4Cell();
      }
    }, new FourNeighborCellConnector());
  }
  
  public static void main(String[] args) throws IOException {
    CellularAutomatonCLI cli = new CellularAutomatonCLI() {
      @Override
      protected CellularAutomaton createCellularAutomaton() throws IOException {
        return new BooleanFunction4(64, 64);
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
