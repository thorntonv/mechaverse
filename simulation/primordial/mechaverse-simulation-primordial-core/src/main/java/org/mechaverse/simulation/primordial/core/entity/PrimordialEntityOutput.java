package org.mechaverse.simulation.primordial.core.entity;

import org.mechaverse.simulation.common.model.MoveDirection;
import org.mechaverse.simulation.common.model.TurnDirection;

/**
 * Encodes and decodes the output of a primordial entity.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class PrimordialEntityOutput {

  public static final MoveDirection[] MOVE_DIRECTIONS = MoveDirection.values();
  public static final TurnDirection[] TURN_DIRECTIONS = TurnDirection.values();

  public static final int DATA_SIZE = 1;

  private static final int MOVE_DIRECTION_IDX = 0;
  private static final int MOVE_DIRECTION_MASK = 0b1;

  private static final int CONSUME_IDX = 0;
  private static final int CONSUME_BIT_IDX = 1;
  private static final int CONSUME_MASK = 0b1 << CONSUME_BIT_IDX;

  private static final int TURN_DIRECTION_IDX = 0;
  private static final int TURN_DIRECTION_BIT_IDX = 2;
  private static final int TURN_DIRECTION_MASK = 0b11 << TURN_DIRECTION_BIT_IDX;


  private int[] data;

  public PrimordialEntityOutput() {
    this(new int[DATA_SIZE]);

    setMoveDirection(MoveDirection.NONE);
    setTurnDirection(TurnDirection.NONE);
  }

  public PrimordialEntityOutput(int[] data) {
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
