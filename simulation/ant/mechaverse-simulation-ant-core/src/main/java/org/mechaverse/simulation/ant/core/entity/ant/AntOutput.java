package org.mechaverse.simulation.ant.core.entity.ant;

import org.mechaverse.simulation.common.model.MoveDirection;
import org.mechaverse.simulation.common.model.TurnDirection;

/**
 * Encodes and decodes the output of an ant.
 */
public final class AntOutput {

  public static final MoveDirection[] MOVE_DIRECTIONS = MoveDirection.values();
  public static final TurnDirection[] TURN_DIRECTIONS = TurnDirection.values();

  public static final int DATA_SIZE = 1;
  public static final int DATA_SIZE_BYTES = DATA_SIZE * 4;

  // TODO(thorntonv) Implement general output.
  // TODO(thorntonv) Implement attack output.

  private static final int MOVE_DIRECTION_IDX = 0;
  private static final int MOVE_DIRECTION_MASK = 0b1;

  private static final int TURN_DIRECTION_IDX = 0;
  private static final int TURN_DIRECTION_BIT_IDX = 1;
  private static final int TURN_DIRECTION_MASK = 0b11 << TURN_DIRECTION_BIT_IDX;

  private static final int PICKUP_OR_DROP_IDX = 0;
  private static final int PICKUP_OR_DROP_BIT_IDX = 3;
  private static final int PICKUP_OR_DROP_MASK = 0b1 << PICKUP_OR_DROP_BIT_IDX;

  private static final int LEAVE_PHEROMONE_IDX = 0;
  private static final int LEAVE_PHEROMONE_BIT_IDX = 4;
  private static final int LEAVE_PHEROMONE_MASK = 0b1 << LEAVE_PHEROMONE_BIT_IDX;

  private static final int PHEROMONE_TYPE_IDX = 0;
  private static final int PHEROMONE_TYPE_BIT_IDX = 5;
  private static final int PHEROMONE_TYPE_MASK = 0b111 << PHEROMONE_TYPE_BIT_IDX;

  private static final int CONSUME_IDX = 0;
  private static final int CONSUME_BIT_IDX = 8;
  private static final int CONSUME_MASK = 0b1 << CONSUME_BIT_IDX;

  private int[] data;

  public AntOutput() {
    this(new int[DATA_SIZE]);

    setMoveDirection(MoveDirection.NONE);
    setTurnDirection(TurnDirection.NONE);
    setPickUpOrDrop(false);
    setLeavePheromone(false);
    setPheromoneType(0);
  }

  public AntOutput(int[] data) {
    this.data = data;
  }

  public MoveDirection getMoveDirection() {
    int moveDirectionOrdinal = (data[MOVE_DIRECTION_IDX] & MOVE_DIRECTION_MASK);
    moveDirectionOrdinal = moveDirectionOrdinal < MOVE_DIRECTIONS.length ? moveDirectionOrdinal : 0;
    return MOVE_DIRECTIONS[moveDirectionOrdinal];
  }

  public void setMoveDirection(MoveDirection moveDirection) {
    if (moveDirection == MoveDirection.BACKWARD) {
      throw new IllegalArgumentException(MoveDirection.BACKWARD.name() + " is not supported");
    }
    int value = moveDirection.ordinal();
    data[MOVE_DIRECTION_IDX] = (data[MOVE_DIRECTION_IDX] & ~MOVE_DIRECTION_MASK) | value;
  }

  public TurnDirection getTurnDirection() {
    int turnDirectionOrdinal =
        (data[TURN_DIRECTION_IDX] & TURN_DIRECTION_MASK) >> TURN_DIRECTION_BIT_IDX;
    turnDirectionOrdinal = turnDirectionOrdinal < TURN_DIRECTIONS.length ? turnDirectionOrdinal : 0;
    return TURN_DIRECTIONS[turnDirectionOrdinal];
  }

  public void setTurnDirection(TurnDirection turnDirection) {
    int value = turnDirection.ordinal();
    data[TURN_DIRECTION_IDX] =
        (data[TURN_DIRECTION_IDX] & ~TURN_DIRECTION_MASK) | (value << TURN_DIRECTION_BIT_IDX);
  }

  public boolean shouldPickUpOrDrop() {
    int pickUpOrDrop = (data[PICKUP_OR_DROP_IDX] & PICKUP_OR_DROP_MASK) >> PICKUP_OR_DROP_BIT_IDX;
    return pickUpOrDrop == 1;
  }

  public void setPickUpOrDrop(boolean pickUpOrDrop) {
    int value = pickUpOrDrop ? 1 : 0;
    data[PICKUP_OR_DROP_IDX] =
        (data[PICKUP_OR_DROP_IDX] & ~PICKUP_OR_DROP_MASK) | (value << PICKUP_OR_DROP_BIT_IDX);
  }

  public boolean shouldLeavePheromone() {
    int leavePheromone =
        (data[LEAVE_PHEROMONE_IDX] & LEAVE_PHEROMONE_MASK) >> LEAVE_PHEROMONE_BIT_IDX;
    return leavePheromone == 1;
  }

  public void setLeavePheromone(boolean leavePheromone) {
    int value = leavePheromone ? 1 : 0;
    data[LEAVE_PHEROMONE_IDX] =
        (data[LEAVE_PHEROMONE_IDX] & ~LEAVE_PHEROMONE_MASK) | (value << LEAVE_PHEROMONE_BIT_IDX);
  }

  public int getPheromoneType() {
    return (data[PHEROMONE_TYPE_IDX] & PHEROMONE_TYPE_MASK) >> PHEROMONE_TYPE_BIT_IDX;
  }

  public void setPheromoneType(int type) {
    data[PHEROMONE_TYPE_IDX] =
        (data[PHEROMONE_TYPE_IDX] & ~PHEROMONE_TYPE_MASK) | (type << PHEROMONE_TYPE_BIT_IDX);
  }

  public boolean shouldConsume() {
    return (data[CONSUME_IDX] & CONSUME_MASK) >> CONSUME_BIT_IDX == 1;
  }

  public void setConsume(boolean shouldConsume) {
    int value = shouldConsume ? 1 : 0;
    data[CONSUME_IDX] = (data[CONSUME_IDX] & ~CONSUME_MASK) | (value << CONSUME_BIT_IDX);
  }

  public int[] getData() {
    return data;
  }

  public void setData(int[] data) {
    this.data = data;
  }

  public void resetToDefault() {
    for (int idx = 0; idx < data.length; idx++) {
      data[idx] = 0;
    }
  }
}
