package org.mechaverse.simulation.primordial.core.environment;

import static org.mechaverse.simulation.common.cellautomaton.CellularAutomatonEntityBehavior.CELLULAR_AUTOMATON_INPUT_MAP_KEY;
import static org.mechaverse.simulation.common.cellautomaton.CellularAutomatonEntityBehavior.CELLULAR_AUTOMATON_OUTPUT_MAP_KEY;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.cellautomaton.SimulationStateCellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorDataSource;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator.CellularAutomatonSimulatorParams;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.MoveDirection;
import org.mechaverse.simulation.common.model.TurnDirection;
import org.mechaverse.simulation.common.util.ArrayUtil;
import org.mechaverse.simulation.common.util.SimulationUtil;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.primordial.core.model.Food;
import org.mechaverse.simulation.primordial.core.model.PrimordialEntityModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel;
import org.mechaverse.simulation.primordial.core.model.PrimordialSimulationModel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * An environment module that updates simulated cellular automata before active entity actions are
 * performed.
 */
public class CellularAutomatonSimulationBehavior extends PrimordialEnvironmentBehavior {

  private static final int MOVE_FORWARD_ORDINAL = MoveDirection.FORWARD.ordinal();
  private static final int TURN_CLOCKWISE_ORDINAL = TurnDirection.CLOCKWISE.ordinal();
  private static final int TURN_COUNTERCLOCKWISE_ORDINAL = TurnDirection.COUNTERCLOCKWISE.ordinal();

  private static final int ENTITY_ORDINAL = EntityType.ENTITY.ordinal();
  private static final int FOOD_ORDINAL = EntityType.FOOD.ordinal();
  private static final int NONE_ORDINAL = EntityType.NONE.ordinal();

  private static final Food FOOD_INSTANCE = new Food();

  @Autowired
  private Function<CellularAutomatonSimulatorParams, CellularAutomatonSimulator> simulatorFactory;
  private CellularAutomatonSimulator simulator;
  private SimulationStateCellularAutomatonDescriptor descriptorDataSource;

  private int numAutomata;
  private int[] entityRows;
  private int[] entityCols;
  private int[] entityEnergy;
  private int[] entityDirections;
  private int[] entityInputs;
  private int[] entityOutputs;

  private static final int[] CELL_DIRECTION_ROW_OFFSETS = new int[]{0, -1, -1, -1, 0, 1, 1, 1};
  private static final int[] CELL_DIRECTION_COL_OFFSETS = new int[]{1, 1, 0, -1, -1, -1, 0, 1};
  private static final int[] TURN_CLOCKWISE = new int[Direction.values().length];
  private static final int[] TURN_COUNTERCLOCKWISE = new int[Direction.values().length];

  static {
    for (Direction direction : Direction.values()) {
      TURN_CLOCKWISE[direction.ordinal()] = SimulationUtil.directionCW(direction).ordinal();
      TURN_COUNTERCLOCKWISE[direction.ordinal()] = SimulationUtil.directionCCW(direction).ordinal();
    }
  }

  private int maxEnergyLevel;
  private Map<EntityModel<EntityType>, Integer> entityModelIndexMap = new HashMap<>();

  private boolean ioMapsSet = false;

  @Override
  public void setState(PrimordialSimulationModel state,
      Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> environment) {
    super.setState(state, environment);
    Preconditions.checkState(state.getEntityMaxCountPerEnvironment() > 0);
    if (simulator == null || descriptorDataSource == null) {
      // Lazily load the cellular automaton simulator.
      descriptorDataSource = new SimulationStateCellularAutomatonDescriptor(state);
      descriptorDataSource.setDefaultDescriptorResourceName("primordial-automaton-descriptor.xml");
      CellularAutomatonSimulatorParams params = new CellularAutomatonSimulatorParams();
      params.numAutomata = state.getEntityMaxCountPerEnvironment();
      params.descriptorDataSource = descriptorDataSource;
      simulator = simulatorFactory.apply(params);

      entityRows = new int[params.numAutomata];
      entityCols = new int[params.numAutomata];
      entityEnergy = new int[params.numAutomata];
      entityDirections = new int[params.numAutomata];
      entityInputs = new int[params.numAutomata * simulator.getAutomatonInputSize()];
      entityOutputs = new int[params.numAutomata * simulator.getAutomatonOutputSize()];

      numAutomata = simulator.size();
      maxEnergyLevel = state.getEntityInitialEnergy();
      generateIOMaps(state);
    }
  }

  @Override
  public void beforePerformAction(PrimordialSimulationModel state,
      Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> environment,
      RandomGenerator random) {
    if (!ioMapsSet) {
      setIOMaps(state);
    }

    final PrimordialEnvironmentModel envModel = environment.getModel();
    final int[][] entityMatrix = envModel.getEntityMatrix();
    final int[][] foodMatrix = envModel.getFoodMatrix();

    final int automatonInputSize = simulator.getAutomatonInputSize();

    for (int idx = 0; idx < numAutomata; idx++) {
      final int entityRow = entityRows[idx];
      final int entityCol = entityCols[idx];
      final int row = entityRow + 1;
      final int col = entityCol + 1;
      final int entityDirectionOrdinal = entityDirections[idx];
      final int frontRow = row + CELL_DIRECTION_ROW_OFFSETS[entityDirectionOrdinal];
      final int frontCol = col + CELL_DIRECTION_COL_OFFSETS[entityDirectionOrdinal];

      // Nearby entity.
      int entitySum = 0;
      entitySum += entityMatrix[row][col];
      entitySum += entityMatrix[row + 1][col];
      entitySum += entityMatrix[row + 1][col - 1];
      entitySum += entityMatrix[row + 1][col + 1];
      entitySum += entityMatrix[row - 1][col];
      entitySum += entityMatrix[row - 1][col - 1];
      entitySum += entityMatrix[row - 1][col + 1];

      // Nearby food.
      int foodSum = 0;
      foodSum += foodMatrix[row][col];
      foodSum += foodMatrix[row + 1][col];
      foodSum += foodMatrix[row + 1][col - 1];
      foodSum += foodMatrix[row + 1][col + 1];
      foodSum += foodMatrix[row - 1][col];
      foodSum += foodMatrix[row - 1][col - 1];
      foodSum += foodMatrix[row - 1][col + 1];

      // Front entity.
      int frontEntityOrdinal = NONE_ORDINAL;
      if (entityMatrix[frontRow][frontCol] != 0) {
        frontEntityOrdinal = ENTITY_ORDINAL;
      } else if (foodMatrix[frontRow][frontCol] != 0) {
        frontEntityOrdinal = FOOD_ORDINAL;
      }

      // Encode input.
      int value = entityEnergy[idx] * 3 / maxEnergyLevel;
      value <<= 2;
      value |= frontEntityOrdinal;
      value <<= 1;
      value |= entitySum > 0 ? 1 : 0;
      value <<= 1;
      value |= foodSum > 0 ? 1 : 0;

      entityInputs[idx * automatonInputSize] = value;
    }

//    simulator.setAutomatonInput(entityInputs);

    simulator.update();
  }

  @Override
  public void afterUpdate(PrimordialSimulationModel state,
      Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> environment,
      RandomGenerator random) {

    final PrimordialEnvironmentModel envModel = environment.getModel();
    final int[][] entityMatrix = envModel.getEntityMatrix();
    final PrimordialEntityModel[][] entityModelMatrix = envModel.getEntityModelMatrix();
    final int[][] foodMatrix = envModel.getFoodMatrix();

    final int maxRow = envModel.getHeight() - 1;
    final int maxCol = envModel.getWidth() - 1;

    final int automatonOutputSize = simulator.getAutomatonOutputSize();

//    simulator.getAutomatonOutput(entityOutputs);

    for (int idx = 0; idx < numAutomata; idx++) {
      final int entityRow = entityRows[idx];
      final int entityCol = entityCols[idx];
      final int row = entityRow + 1;
      final int col = entityCol + 1;
      final int directionOrdinal = entityDirections[idx];
      final int frontRow = row + CELL_DIRECTION_ROW_OFFSETS[directionOrdinal];
      final int frontCol = col + CELL_DIRECTION_COL_OFFSETS[directionOrdinal];

      int energy = entityEnergy[idx] - 1;

      if (energy <= 0) {
        if (entityMatrix[row][col] != 0) {
          PrimordialEntityModel entityModel = entityModelMatrix[row][col];
          environment.removeEntity(entityModel);
          entityModelMatrix[row][col] = null;
          entityMatrix[row][col] = 0;
          simulator.getAllocator().deallocate(idx);
        }
        continue;
      }

      // Decode output.
      int outputValue = entityOutputs[idx * automatonOutputSize];

      final int moveDirectionOrdinal = outputValue & 0b1;
      outputValue >>= 1;
      final int turnDirectionOrdinal = outputValue & 0b11;
      outputValue >>= 2;
      final boolean consume = outputValue > 0;

      // Consume action.
      if (consume && foodMatrix[row][col] == 1) {
        envModel.removeFood(entityRow, entityCol);
        environment.removeEntity(FOOD_INSTANCE);
        energy += 100;
      }

      // Move action.
      if (moveDirectionOrdinal == MOVE_FORWARD_ORDINAL && entityMatrix[frontRow][frontCol] == 0 &&
          frontRow > 0 && frontCol > 0 && frontRow < maxRow && frontCol < maxCol) {

        assert entityModelMatrix[row][col] != null;
        assert entityMatrix[row][col] == 1;
        assert entityModelMatrix[frontRow][frontCol] == null;

        entityModelMatrix[frontRow][frontCol] = entityModelMatrix[row][col];
        entityModelMatrix[row][col] = null;
        entityMatrix[row][col] = 0;
        entityMatrix[frontRow][frontCol] = 1;
      }

      // Turn action.
      if (turnDirectionOrdinal == TURN_CLOCKWISE_ORDINAL) {
        entityDirections[idx] = TURN_CLOCKWISE[directionOrdinal];
      } else if (turnDirectionOrdinal == TURN_COUNTERCLOCKWISE_ORDINAL) {
        entityDirections[idx] = TURN_CLOCKWISE[directionOrdinal];
      }

      entityEnergy[idx] = energy;
    }
  }

  @Override
  public void onAddEntity(EntityModel<EntityType> entity,
      PrimordialSimulationModel simulationModel,
      PrimordialEnvironmentModel environmentModel) {
    if (entity.getType() != EntityType.ENTITY) {
      return;
    }
    int entityIdx = simulator.getAllocator().allocate();
    entityRows[entityIdx] = entity.getY();
    entityCols[entityIdx] = entity.getX();
    entityDirections[entityIdx] = 0;
    entityEnergy[entityIdx] = entity.getEnergy();
    entityModelIndexMap.put(entity, entityIdx);
  }

  @Override
  public void onRemoveEntity(EntityModel<EntityType> entity,
      PrimordialSimulationModel simulationModel,
      PrimordialEnvironmentModel environmentModel) {
    if (entity.getType() != EntityType.ENTITY) {
      return;
    }

    Integer entityIdx = entityModelIndexMap.get(entity);
    if (entityIdx != null) {
      simulator.getAllocator().deallocate(entityIdx);
    }
  }

  public CellularAutomatonDescriptorDataSource getDescriptorDataSource() {
    return descriptorDataSource;
  }

  public CellularAutomatonSimulator getSimulator() {
    return simulator;
  }

  @Override
  public void onClose() throws Exception {
    super.onClose();
    simulator.close();
  }

  private void generateIOMaps(PrimordialSimulationModel state) {
    RandomGenerator random = new Well19937c(0x12345);
    if (!state.dataContainsKey(CELLULAR_AUTOMATON_INPUT_MAP_KEY)) {
      int[] inputMap = new int[simulator.getAutomatonInputMapSize()];
      for (int idx = 0; idx < inputMap.length; idx++) {
        inputMap[idx] = random
            .nextInt(descriptorDataSource.getSimulationModel().getCellOutputStateSize());
      }
      state.putData(CELLULAR_AUTOMATON_INPUT_MAP_KEY, ArrayUtil.toByteArray(inputMap));
    }

    if (!state.dataContainsKey(CELLULAR_AUTOMATON_OUTPUT_MAP_KEY)) {
      int[] outputMap = new int[simulator.getAutomatonOutputMapSize()];
      for (int idx = 0; idx < outputMap.length; idx++) {
        outputMap[idx] = random
            .nextInt(descriptorDataSource.getSimulationModel().getCellOutputStateSize());
      }
      state.putData(CELLULAR_AUTOMATON_OUTPUT_MAP_KEY, ArrayUtil.toByteArray(outputMap));
    }
  }

  private void setIOMaps(PrimordialSimulationModel state) {
    Preconditions.checkState(state.dataContainsKey(CELLULAR_AUTOMATON_INPUT_MAP_KEY));
    Preconditions.checkState(state.dataContainsKey(CELLULAR_AUTOMATON_OUTPUT_MAP_KEY));

    for (int automatonIdx = 0; automatonIdx < simulator.size(); automatonIdx++) {
      simulator.setAutomatonInputMap(automatonIdx,
          ArrayUtil.toIntArray(state.getData(CELLULAR_AUTOMATON_INPUT_MAP_KEY)));
      simulator.setAutomatonOutputMap(automatonIdx,
          ArrayUtil.toIntArray(state.getData(CELLULAR_AUTOMATON_OUTPUT_MAP_KEY)));
    }
    ioMapsSet = true;
  }
}
