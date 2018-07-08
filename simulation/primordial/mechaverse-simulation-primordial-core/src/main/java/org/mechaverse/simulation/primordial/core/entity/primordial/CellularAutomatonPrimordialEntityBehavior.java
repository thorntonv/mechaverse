package org.mechaverse.simulation.primordial.core.entity.primordial;

import java.util.function.Function;
import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataGenerator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorDataSource;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.SimulatorCellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonVisualizer;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;
import org.mechaverse.simulation.common.util.ArrayUtil;
import org.mechaverse.simulation.primordial.core.PrimordialSimulationState;
import org.mechaverse.simulation.primordial.core.entity.primordial.ActivePrimordialEntity.PrimordialEntityBehavior;
import org.mechaverse.simulation.primordial.core.model.PrimordialEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link PrimordialEntityBehavior} implementation that is based on a simulated cellular automaton.
 */
public class CellularAutomatonPrimordialEntityBehavior implements PrimordialEntityBehavior {

  public static final String AUTOMATON_STATE_KEY = "cellularAutomatonState";

  private static final Color DARK_BLUE = Color.BLUE.darker().darker();
  private static final Color DARK_GREEN = Color.GREEN.darker().darker().darker();

  private static final Logger logger = LoggerFactory.getLogger(CellularAutomatonPrimordialEntityBehavior.class);

  private PrimordialEntity entity;
  private final int automatonIndex;
  private final int[] entityOutputData;
  private final int[] automatonOutputData;
  private int[] bitOutputMap;
  private final PrimordialEntityOutput output = new PrimordialEntityOutput();
  private final int[] automatonState;
  private SimulationDataStore dataStore;
  private GeneticDataStore geneticDataStore;
  private boolean stateSet = false;
  private final CellularAutomatonSimulationModel model;
  private final CellularAutomatonSimulator simulator;
  private CellularAutomatonGeneticDataGenerator geneticDataGenerator =
      new CellularAutomatonGeneticDataGenerator();

  private static SimulatorCellularAutomaton cellularAutomaton;
  private static CellularAutomatonVisualizer visualizer;

  public CellularAutomatonPrimordialEntityBehavior(
      CellularAutomatonDescriptorDataSource dataSource, CellularAutomatonSimulator simulator) {
    this.model = dataSource.getSimulationModel();
    this.simulator = simulator;

    this.entityOutputData = new int[PrimordialEntityOutput.DATA_SIZE];
    this.automatonOutputData = new int[simulator.getAutomatonOutputSize()];
    this.bitOutputMap = new int[simulator.getAutomatonOutputSize()];
    this.automatonIndex = simulator.getAllocator().allocate();
    this.automatonState = new int[simulator.getAutomatonStateSize()];
  }

  @Override
  public void setEntity(PrimordialEntity entity) {
    this.entity = entity;
  }

  @Override
  public void setInput(PrimordialEntityInput input, RandomGenerator random) {
    if (!stateSet) {
      if (geneticDataStore.size() == 0) {
        generateGeneticData(random);
      }

      initializeCellularAutomaton();
      stateSet = true;
    }

    simulator.setAutomatonInput(automatonIndex, input.getData());
  }

  @Override
  public PrimordialEntityOutput getOutput(RandomGenerator random) {
    updateEntityOutputData();
    if(cellularAutomaton != null && visualizer != null) {
      cellularAutomaton.refresh();
      visualizer.repaintUI();
    }
    output.setData(entityOutputData);
    return output;
  }

  @Override
  public void onRemoveEntity() {
    simulator.getAllocator().deallocate(automatonIndex);
    stateSet = false;

    // Remove entity state.
    if (dataStore != null) {
      dataStore.clear();
    }
    if (geneticDataStore != null) {
      geneticDataStore.clear();
    }
  }

  @Override
  public void setState(PrimordialSimulationState state) {
    this.dataStore = state.getEntityDataStore(entity);
    this.geneticDataStore = state.getEntityGeneticDataStore(entity);

    // If the primordial entity has an existing automaton state then load it.
    byte[] stateBytes = dataStore.get(AUTOMATON_STATE_KEY);
    if (stateBytes != null) {
      int[] automatonState = ArrayUtil.toIntArray(stateBytes);
      simulator.setAutomatonState(automatonIndex, automatonState);
      stateSet = true;
      logger.trace("setState {} automatonState = {}", entity.getId(),
          Arrays.hashCode(automatonState));
    }
  }

  @Override
  public void updateState(PrimordialSimulationState state) {
    if(stateSet) {
      simulator.getAutomatonState(automatonIndex, automatonState);
      state.getEntityDataStore(entity)
          .put(AUTOMATON_STATE_KEY, ArrayUtil.toByteArray(automatonState));
      logger.trace("updateState {} automatonState = {}", entity.getId(),
          Arrays.hashCode(automatonState));
    }
  }

  private void generateGeneticData(RandomGenerator random) {
    geneticDataGenerator.generateGeneticData(geneticDataStore, model,
        simulator.getAutomatonOutputSize(), random);
  }

  private void initializeCellularAutomaton() {
    // Cellular automaton state.
    int[] automatonState = geneticDataGenerator.getCellularAutomatonState(geneticDataStore);
    simulator.setAutomatonState(automatonIndex, automatonState);
    dataStore.put(AUTOMATON_STATE_KEY, ArrayUtil.toByteArray(automatonState));
    logger.trace("initializeCellularAutomaton {} automatonState = {}", entity.getId(),
        Arrays.hashCode(automatonState));

    SimulatorCellularAutomaton cells = new SimulatorCellularAutomaton(model, simulator, automatonIndex);
    // Cellular automaton input map.
    final List<SimulatorCellularAutomaton.SimulatorCellularAutomatonCell> inputCells = ImmutableList.of(
        cells.getCell(2, 2),
        cells.getCell(2, 7),
        cells.getCell(7, 2),
        cells.getCell(7, 7)
    );
    for(SimulatorCellularAutomaton.SimulatorCellularAutomatonCell inputCell : inputCells) {
      inputCell.addOutputToInputMap(0);
    }
    cells.updateInputMap();

    // Cellular automaton output map.
    final List<SimulatorCellularAutomaton.SimulatorCellularAutomatonCell> outputCells = ImmutableList.of(
        cells.getCell(0, 0),
        cells.getCell(0, 9),
        cells.getCell(9, 0),
        cells.getCell(9, 9)
    );
    for(SimulatorCellularAutomaton.SimulatorCellularAutomatonCell outputCell : outputCells) {
      outputCell.addOutputToOutputMap(0);
    }
    cells.updateOutputMap();

    setBitOutputMap(new int[]{0, 0, 0, 0});

    if(automatonIndex == 0) {
      if(visualizer != null) {
        visualizer.dispose();
        visualizer = null;
        cellularAutomaton = null;
      }
      cellularAutomaton = cells;
      cellularAutomaton.refresh();

      Function<CellularAutomaton.Cell, Color> CELL_COLOR_PROVIDER =
          cell -> {
            Color zeroColor = Color.BLACK;
            Color oneColor = Color.WHITE;
            if(inputCells.contains(cell)){
              zeroColor = DARK_BLUE;
              oneColor = Color.BLUE;
            }
            if(outputCells.contains(cell)) {
              zeroColor = DARK_GREEN;
              oneColor = Color.GREEN;
            }
            return (cell.getOutput(0) & 1) == 0 ? zeroColor : oneColor;
          };
      visualizer = new CellularAutomatonVisualizer(cellularAutomaton, CELL_COLOR_PROVIDER, 800, 600, 0);
      visualizer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      visualizer.setLocation(0, 0);
    }
  }

  private void updateEntityOutputData() {
    simulator.getAutomatonOutput(automatonIndex, automatonOutputData);
    int entityOutputIdx = -1;
    for (int idx = 0; idx < automatonOutputData.length; idx++) {
      int bitPosition = idx % Integer.SIZE;
      if (bitPosition == 0) {
        entityOutputIdx++;
        entityOutputData[entityOutputIdx] = 0;
      }

      // Isolate the selected bit of the automaton output value and or it into the primordial output data.
      entityOutputData[entityOutputIdx] |=
          ((automatonOutputData[idx] >> bitOutputMap[idx]) & 0b1) << bitPosition;
    }
  }

  private void setBitOutputMap(int[] bitOutputMap) {
    for (int idx = 0; idx < bitOutputMap.length; idx++) {
      bitOutputMap[idx] = Math.abs(bitOutputMap[idx]) % 32;
    }
    this.bitOutputMap = bitOutputMap;
  }
}
