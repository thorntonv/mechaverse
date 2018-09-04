package org.mechaverse.simulation.primordial.core.environment;

import static org.mechaverse.simulation.common.cellautomaton.CellularAutomatonEntityBehavior.CELLULAR_AUTOMATON_INPUT_MAP_KEY;
import static org.mechaverse.simulation.common.cellautomaton.CellularAutomatonEntityBehavior.CELLULAR_AUTOMATON_OUTPUT_MAP_KEY;
import static org.mechaverse.simulation.common.cellautomaton.CellularAutomatonEntityBehavior.CELLULAR_AUTOMATON_STATE_KEY;
import static org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticDataGenerator.CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.simulation.common.Environment;
import org.mechaverse.simulation.common.cellautomaton.SimulationStateCellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator.CellularAutomatonSimulatorParams;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.MoveDirection;
import org.mechaverse.simulation.common.model.TurnDirection;
import org.mechaverse.simulation.common.util.ArrayUtil;
import org.mechaverse.simulation.common.util.SimulationModelUtil;
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

  public static int AUTOMATON_INPUT_DATA_SIZE_BITS = 6;
  public static int AUTOMATON_OUTPUT_DATA_SIZE_BITS = 4;

  private static final int MOVE_FORWARD_ORDINAL = MoveDirection.FORWARD.ordinal();
  private static final int TURN_CLOCKWISE_ORDINAL = TurnDirection.CLOCKWISE.ordinal();
  private static final int TURN_COUNTERCLOCKWISE_ORDINAL = TurnDirection.COUNTERCLOCKWISE.ordinal();

  private static final int ENTITY_ORDINAL = EntityType.ENTITY.ordinal();
  private static final int FOOD_ORDINAL = EntityType.FOOD.ordinal();
  private static final int NONE_ORDINAL = EntityType.NONE.ordinal();

  private static final Food FOOD_INSTANCE = new Food();

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

  @Autowired
  private Function<CellularAutomatonSimulatorParams, CellularAutomatonSimulator> simulatorFactory;
  private CellularAutomatonSimulator simulator;
  private SimulationStateCellularAutomatonDescriptor descriptorDataSource;

  private int numAutomata;
  private int[] entityRows;
  private int[] entityCols;
  private int[] entityEnergy;
  private int[] entityDirections;

  private int maxEnergyLevel;
  private final Set<EntityModel<EntityType>> newEntities = Sets.newLinkedHashSet();
  private final Map<EntityModel<EntityType>, Integer> entityModelIndexMap = Maps.newIdentityHashMap();
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

      numAutomata = simulator.size();
      entityRows = new int[numAutomata];
      entityCols = new int[numAutomata];
      entityEnergy = new int[numAutomata];
      entityDirections = new int[numAutomata];

      maxEnergyLevel = state.getEntityInitialEnergy();
      generateIOMaps(state);
    }
  }

  @Override
  public void updateState(PrimordialSimulationModel state,
      Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> environment) {
    super.updateState(state, environment);
    for (Map.Entry<EntityModel<EntityType>, Integer> entry : entityModelIndexMap.entrySet()) {
      EntityModel<EntityType> entityModel = entry.getKey();
      int automatonIdx = entry.getValue();
      entityModel.setY(entityRows[automatonIdx]);
      entityModel.setX(entityCols[automatonIdx]);
      entityModel.setEnergy(entityEnergy[automatonIdx]);
      entityModel.setDirection(SimulationModelUtil.DIRECTIONS[entityDirections[automatonIdx]]);
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
    final int[] inputData = new int[1];

    // Add new entities.
    for(EntityModel<EntityType> entityModel : newEntities) {
      initEntity(entityModel, random);
    }
    newEntities.clear();

    for (int idx = 0; idx < numAutomata; idx++) {
      final int energy = entityEnergy[idx];
      if (energy <= 0) {
        continue;
      }
      final int entityRow = entityRows[idx];
      final int entityCol = entityCols[idx];
      final int row = entityRow + 1;
      final int col = entityCol + 1;
      final int entityDirectionOrdinal = entityDirections[idx];
      final int frontRow = row + CELL_DIRECTION_ROW_OFFSETS[entityDirectionOrdinal];
      final int frontCol = col + CELL_DIRECTION_COL_OFFSETS[entityDirectionOrdinal];

      int leftCol = col - 1;

      // Nearby entity.
      int nearbyEntity = 0;

      int r = row - 1;
      int[] matrixRow = entityMatrix[r];
      int c = leftCol;
      nearbyEntity |= matrixRow[c];
      c++;
      nearbyEntity |= matrixRow[c];
      c++;
      nearbyEntity |= matrixRow[c];

      r++;
      c = leftCol;
      matrixRow = entityMatrix[r];
      nearbyEntity |= matrixRow[c];
      c += 2;
      nearbyEntity |= matrixRow[c];

      r++;
      c = leftCol;
      matrixRow = entityMatrix[r];
      nearbyEntity |= matrixRow[c];
      c++;
      nearbyEntity |= matrixRow[c];
      c++;
      nearbyEntity |= matrixRow[c];

      // Nearby food.
      int nearbyFood = 0;

      r = row - 1;
      matrixRow = foodMatrix[r];
      c = leftCol;
      nearbyFood |= matrixRow[c];
      c++;
      nearbyFood |= matrixRow[c];
      c++;
      nearbyFood |= matrixRow[c];

      r++;
      matrixRow = foodMatrix[r];
      c = leftCol;
      nearbyFood |= matrixRow[c];
      c++;
      nearbyFood |= matrixRow[c];
      c++;
      nearbyFood |= matrixRow[c];

      r++;
      matrixRow = foodMatrix[r];
      c = leftCol;
      nearbyFood |= matrixRow[c];
      c++;
      nearbyFood |= matrixRow[c];
      c++;
      nearbyFood |= matrixRow[c];

      // Front entity.
      int frontEntityOrdinal = NONE_ORDINAL;
      if (entityMatrix[frontRow][frontCol] != 0) {
        frontEntityOrdinal = ENTITY_ORDINAL;
      } else if (foodMatrix[frontRow][frontCol] != 0) {
        frontEntityOrdinal = FOOD_ORDINAL;
      }

      // Encode input.
      int value = energy * 3 / maxEnergyLevel;
      value <<= 2;
      value |= frontEntityOrdinal;
      value <<= 1;
      value |= nearbyEntity;
      value <<= 1;
      value |= nearbyFood;

      inputData[0] = value;
      simulator.setAutomatonInput(idx, inputData);
    }

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
    final int foodEnergy = state.getFoodInitialEnergy();

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
          entityModel.setY(entityRow);
          entityModel.setX(entityCol);
          environment.removeEntity(entityModel);
        }
        continue;
      }

      assert entityMatrix[row][col] == 1 : "" + energy;
      assert entityModelMatrix[row][col] != null;

      // Decode output.
      int[] outputData = new int[1];
      simulator.getAutomatonOutput(idx, outputData);
      int outputValue = outputData[0];

      final int moveDirectionOrdinal = outputValue & 0b1;
      outputValue >>= 1;
      final int turnDirectionOrdinal = outputValue & 0b11;
      outputValue >>= 2;
      final boolean consume = outputValue > 0;

      // Consume action.
      if (consume && foodMatrix[row][col] == 1) {
        envModel.removeFood(entityRow, entityCol);
        environment.removeEntity(FOOD_INSTANCE);
        energy += foodEnergy;
      }

      // Move action.
      if (moveDirectionOrdinal == MOVE_FORWARD_ORDINAL && entityMatrix[frontRow][frontCol] == 0 &&
          frontRow > 0 && frontCol > 0 && frontRow < maxRow && frontCol < maxCol) {
        entityModelMatrix[frontRow][frontCol] = entityModelMatrix[row][col];
        entityModelMatrix[row][col] = null;
        entityMatrix[row][col] = 0;
        entityMatrix[frontRow][frontCol] = 1;
        entityRows[idx] = frontRow - 1;
        entityCols[idx] = frontCol - 1;
      }

      // Turn action.
      if (turnDirectionOrdinal == TURN_CLOCKWISE_ORDINAL) {
        entityDirections[idx] = TURN_CLOCKWISE[directionOrdinal];
      } else if (turnDirectionOrdinal == TURN_COUNTERCLOCKWISE_ORDINAL) {
        entityDirections[idx] = TURN_COUNTERCLOCKWISE[directionOrdinal];
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
    newEntities.add(entity);
  }

  @Override
  public void onRemoveEntity(EntityModel<EntityType> entity,
      PrimordialSimulationModel simulationModel,
      PrimordialEnvironmentModel environmentModel) {
    if (entity.getType() != EntityType.ENTITY) {
      return;
    }

    Integer entityIdx = entityModelIndexMap.remove(entity);
    if (entityIdx != null) {
      simulator.getAllocator().deallocate(entityIdx);
      entity.setY(entityRows[entityIdx]);
      entity.setX(entityCols[entityIdx]);
      environmentModel.remove(entity);
      entityEnergy[entityIdx] = 0;
    }
    newEntities.remove(entity);
  }

  @Override
  public void onClose() throws Exception {
    super.onClose();
    simulator.close();
  }

  private void initEntity(EntityModel<EntityType> entity, RandomGenerator random) {
    int entityIdx = simulator.getAllocator().allocate();
    entityEnergy[entityIdx] = entity.getEnergy();
    entityRows[entityIdx] = entity.getY();
    entityCols[entityIdx] = entity.getX();
    entityDirections[entityIdx] = entity.getDirection() != null ?
        entity.getDirection().ordinal() : 0;
    entityModelIndexMap.put(entity, entityIdx);
    entity.setX(-1);
    entity.setY(-1);

    // If the entity has an existing automaton state then load it.
    byte[] stateBytes = entity.getData(CELLULAR_AUTOMATON_STATE_KEY);
    if (stateBytes != null) {
        simulator.setAutomatonState(entityIdx, ArrayUtil.toIntArray(stateBytes));
    } else {
      int[] automatonState = new int[simulator.getAutomatonStateSize()];
      for (int idx = 0; idx < automatonState.length; idx++) {
        automatonState[idx] = random.nextInt();
      }
      entity.putData(CELLULAR_AUTOMATON_STATE_GENETIC_DATA_KEY,
          ArrayUtil.toByteArray(automatonState));
      simulator.setAutomatonState(entityIdx, automatonState);
    }
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
