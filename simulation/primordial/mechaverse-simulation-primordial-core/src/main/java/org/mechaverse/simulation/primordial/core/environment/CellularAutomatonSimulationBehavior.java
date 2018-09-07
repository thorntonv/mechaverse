package org.mechaverse.simulation.primordial.core.environment;

import static org.mechaverse.simulation.common.cellautomaton.CellularAutomatonEntityBehavior.CELLULAR_AUTOMATON_INPUT_MAP_KEY;
import static org.mechaverse.simulation.common.cellautomaton.CellularAutomatonEntityBehavior.CELLULAR_AUTOMATON_OUTPUT_MAP_KEY;
import static org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel.ENTITY_MASK;
import static org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel.FOOD_ENTITY_MASK;
import static org.mechaverse.simulation.primordial.core.model.PrimordialEnvironmentModel.GENETIC_DATA_KEY;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
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
  private final TIntObjectMap<EntityModel<EntityType>> automatonIdxEntityModelMap = new TIntObjectHashMap<>();
  private final Map<EntityModel<EntityType>, Integer> entityModelAutomatonIdxMap = Maps.newIdentityHashMap();


  private int[] stateData;
  private int[] inputData;
  private int[] outputData;

  private int maxEnergyLevel;
  private final Set<EntityModel<EntityType>> newEntities = Sets.newLinkedHashSet();
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
      for(int idx = 0; idx < numAutomata; idx++) {
        entityRows[idx] = -1;
        entityCols[idx] = -1;
      }

      stateData = new int[simulator.getAutomatonStateSize() * numAutomata];
      inputData = new int[simulator.getAutomatonInputSize() * numAutomata];
      outputData = new int[simulator.getAutomatonOutputSize() * numAutomata];

      maxEnergyLevel = state.getEntityInitialEnergy();
      generateIOMaps(state);
    }
  }

  @Override
  public void updateState(PrimordialSimulationModel state,
      Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> environment) {
    super.updateState(state, environment);
    for (Map.Entry<EntityModel<EntityType>, Integer> entry : entityModelAutomatonIdxMap.entrySet()) {
      EntityModel<EntityType> entityModel = entry.getKey();
      int automatonIdx = entry.getValue();
      entityModel.setY(entityRows[automatonIdx]);
      entityModel.setX(entityCols[automatonIdx]);
      entityModel.setEnergy(entityEnergy[automatonIdx]);
      entityModel.setDirection(SimulationModelUtil.DIRECTIONS[entityDirections[automatonIdx]]);
      entityModel.putData(GENETIC_DATA_KEY, ArrayUtil.toByteArray(environment.getModel().getEntityGeneticData(entityModel)));
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
    final byte[][] entityMatrix = envModel.getEntityMatrix();

    // Add new entities.

    if(newEntities.size() > 0) {
      simulator.getAutomataState(stateData);
    }

    for(EntityModel<EntityType> entityModel : newEntities) {
      initEntity(entityModel, envModel, random);
    }

    if (newEntities.size() > 0) {
      simulator.setAutomataState(stateData);
      newEntities.clear();
    }

    for (int idx = 0; idx < numAutomata; ) {
      int i1 = 0;
      int i2 = 0;
      int i3 = 0;
      int i4 = 0;
      int i5 = 0;
      int i6 = 0;
      final int automatonInputIdx = idx / Integer.SIZE;

      final int maxCount = Math.min(32, numAutomata - idx);
      for (int bitOffset = 0; bitOffset < maxCount; bitOffset++) {
        final int energy = entityEnergy[idx];
        if (energy <= 0) {
          idx++;
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

        // Nearby entity / food.
        int nearbyEntity = 0;
        int nearbyFood = 0;

        int r = row - 1;
        int c = leftCol;
        byte[] entityMatrixRow = entityMatrix[r];
        byte tmp = entityMatrixRow[c];
        nearbyEntity |= tmp & ENTITY_MASK;
        nearbyFood |= tmp & FOOD_ENTITY_MASK;
        c++;
        tmp = entityMatrixRow[c];
        nearbyEntity |= tmp & ENTITY_MASK;
        nearbyFood |= tmp & FOOD_ENTITY_MASK;
        c++;
        tmp = entityMatrixRow[c];
        nearbyEntity |= tmp & ENTITY_MASK;
        nearbyFood |= tmp & FOOD_ENTITY_MASK;

        r++;
        c = leftCol;
        entityMatrixRow = entityMatrix[r];
        tmp = entityMatrixRow[c];
        nearbyEntity |= tmp & ENTITY_MASK;
        nearbyFood |= tmp & FOOD_ENTITY_MASK;
        c++;
        // Skip checking the entity itself.
        nearbyFood |= entityMatrixRow[c] & FOOD_ENTITY_MASK;
        c++;
        tmp = entityMatrixRow[c];
        nearbyEntity |= tmp & ENTITY_MASK;
        nearbyFood |= tmp & FOOD_ENTITY_MASK;

        r++;
        c = leftCol;
        entityMatrixRow = entityMatrix[r];
        tmp = entityMatrixRow[c];
        nearbyEntity |= tmp & ENTITY_MASK;
        nearbyFood |= tmp & FOOD_ENTITY_MASK;
        c++;
        tmp = entityMatrixRow[c];
        nearbyEntity |= tmp & ENTITY_MASK;
        nearbyFood |= tmp & FOOD_ENTITY_MASK;
        c++;
        tmp = entityMatrixRow[c];
        nearbyEntity |= tmp & ENTITY_MASK;
        nearbyFood |= tmp & FOOD_ENTITY_MASK;
        nearbyFood >>= 1;

        // Front entity.
        int frontEntityOrdinal = NONE_ORDINAL;
        tmp = entityMatrix[frontRow][frontCol];
        if ((tmp & ENTITY_MASK) != 0) {
          frontEntityOrdinal = ENTITY_ORDINAL;
        } else if ((tmp & FOOD_ENTITY_MASK) != 0) {
          frontEntityOrdinal = FOOD_ORDINAL;
        }

        // Encode input.
        int value = energy * 3 / maxEnergyLevel;
        i1 |= (value & 0b1) << bitOffset;
        i2 |= (value >> 1) << bitOffset;
        value = frontEntityOrdinal;
        i3 |= (value & 0b1) << bitOffset;
        i4 |= (value >> 1) << bitOffset;
        i5 |= (nearbyEntity & 0b1) << bitOffset;
        i6 |= (nearbyFood & 0b1) << bitOffset;

        idx++;
      }

      inputData[automatonInputIdx] = i1;
      inputData[automatonInputIdx + 1] = i2;
      inputData[automatonInputIdx + 2] = i3;
      inputData[automatonInputIdx + 3] = i4;
      inputData[automatonInputIdx + 4] = i5;
      inputData[automatonInputIdx + 5] = i6;
    }

    simulator.setAutomataInput(inputData);

    simulator.update();
  }

  @Override
  public void afterUpdate(PrimordialSimulationModel state,
      Environment<PrimordialSimulationModel, PrimordialEnvironmentModel, EntityModel<EntityType>, EntityType> environment,
      RandomGenerator random) {

    final PrimordialEnvironmentModel envModel = environment.getModel();
    final byte[][] entityMatrix = envModel.getEntityMatrix();

    final int maxRow = envModel.getHeight() - 1;
    final int maxCol = envModel.getWidth() - 1;
    final int foodEnergy = state.getFoodInitialEnergy();

    simulator.getAutomataOutput(outputData);

    for (int idx = 0; idx < numAutomata; ) {
      final int automatonOutputIdx = idx / Integer.SIZE;
      int o1 = outputData[automatonOutputIdx];
      int o2 = outputData[automatonOutputIdx + 1];
      int o3 = outputData[automatonOutputIdx + 2];
      int o4 = outputData[automatonOutputIdx + 3];

      final int maxCount = Math.min(32, numAutomata - idx);
      for (int bitOffset = 0; bitOffset < maxCount; bitOffset++) {
        final int entityRow = entityRows[idx];
        final int entityCol = entityCols[idx];
        final int row = entityRow + 1;
        final int col = entityCol + 1;
        final int directionOrdinal = entityDirections[idx];
        final int frontRow = row + CELL_DIRECTION_ROW_OFFSETS[directionOrdinal];
        final int frontCol = col + CELL_DIRECTION_COL_OFFSETS[directionOrdinal];

        int energy = entityEnergy[idx] - 1;

        if (energy <= 0) {
          if (entityRow >= 0 && entityCol >= 0 && (entityMatrix[row][col] & ENTITY_MASK) > 0) {
            EntityModel<EntityType> entityModel = automatonIdxEntityModelMap.get(idx);
            entityModel.setY(entityRow);
            entityModel.setX(entityCol);
            environment.removeEntity(entityModel);
          }
          idx++;
          continue;
        }

        assert (entityMatrix[row][col] & ENTITY_MASK) > 0;
        assert automatonIdxEntityModelMap.containsKey(idx);

        // Decode output.
        final int moveDirectionOrdinal = (o1 >> bitOffset) & 0b1;
        final int turnDirectionOrdinal = (((o2 >> bitOffset) & 0b1) << 1) | ((o3 >> bitOffset) & 0b1);
        final boolean consume = ((o4 >> bitOffset) & 0b1) > 0;

        // Consume action.
        if (consume && (entityMatrix[row][col] & FOOD_ENTITY_MASK) > 0) {
          envModel.removeFood(entityRow, entityCol);
          environment.removeEntity(FOOD_INSTANCE);
          energy += foodEnergy;
        }

        // Move action.
        if (moveDirectionOrdinal == MOVE_FORWARD_ORDINAL
            && (entityMatrix[frontRow][frontCol] & ENTITY_MASK) == 0 &&
            frontRow > 0 && frontCol > 0 && frontRow < maxRow && frontCol < maxCol) {
          entityMatrix[row][col] &= ~ENTITY_MASK;
          entityMatrix[frontRow][frontCol] |= ENTITY_MASK;
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

        idx++;
      }
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

    Integer entityAutomatonIdx = entityModelAutomatonIdxMap.remove(entity);
    if (entityAutomatonIdx != null) {
      simulator.getAllocator().deallocate(entityAutomatonIdx);
      entity.setY(entityRows[entityAutomatonIdx]);
      entity.setX(entityCols[entityAutomatonIdx]);
      environmentModel.remove(entity);
      entityEnergy[entityAutomatonIdx] = 0;
    }
    newEntities.remove(entity);
  }

  @Override
  public void onClose() throws Exception {
    super.onClose();
    simulator.close();
  }

  private void initEntity(final EntityModel<EntityType> entity,
      final PrimordialEnvironmentModel environmentModel, final RandomGenerator random) {
    int entityIdx = simulator.getAllocator().allocate();
    entityEnergy[entityIdx] = entity.getEnergy();
    entityRows[entityIdx] = entity.getY();
    entityCols[entityIdx] = entity.getX();
    entityDirections[entityIdx] = entity.getDirection() != null ?
        entity.getDirection().ordinal() : 0;
    entityModelAutomatonIdxMap.put(entity, entityIdx);
    automatonIdxEntityModelMap.put(entityIdx, entity);
    entity.setX(-1);
    entity.setY(-1);

    // If the entity has an existing automaton state then load it.
    int[] state = environmentModel.getEntityGeneticData(entity);

    final int automatonStateSize = simulator.getAutomatonStateSize();
    if (state == null) {
      state = new int[automatonStateSize];
      for (int i = 0; i < state.length; i++) {
        state[i] = random.nextInt();
      }
      environmentModel.setEntityGeneticData(entity, state);
    }

    final int automatonIdx = entityIdx / Integer.SIZE;
    final int bitOffset = entityIdx % Integer.SIZE;
    final int mask = ~(1 << bitOffset);
    final int startIdx = automatonIdx * automatonStateSize;

    for (int i = 0; i < state.length; i++) {
      stateData[startIdx + i] &= mask;
      stateData[startIdx + i] |= (state[i] & 0b1) << bitOffset;
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
