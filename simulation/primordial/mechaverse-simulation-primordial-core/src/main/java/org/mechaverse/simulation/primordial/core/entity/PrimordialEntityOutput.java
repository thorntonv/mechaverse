package org.mechaverse.simulation.primordial.core.entity;

import com.google.common.math.IntMath;
import java.math.RoundingMode;
import org.mechaverse.simulation.common.model.MoveDirection;
import org.mechaverse.simulation.common.model.TurnDirection;

/**
 * Encodes and decodes the output of a primordial entity.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class PrimordialEntityOutput {

  public static final MoveDirection[] MOVE_DIRECTIONS = MoveDirection.values();
  public static final TurnDirection[] TURN_DIRECTIONS = TurnDirection.values();

  public static final int DATA_SIZE_BITS = 4;

  private static final int MOVE_DIRECTION_IDX = 0;
  private static final int MOVE_DIRECTION_MASK = 0b1;

  private static final int CONSUME_IDX = 1;
  private static final int CONSUME_BIT_IDX = 0;
  private static final int CONSUME_MASK = 0b1;

  private static final int TURN_DIRECTION_IDX = 2;
  private static final int TURN_DIRECTION_BIT_IDX = 0;
  private static final int TURN_DIRECTION_MASK = 0b1;


  private int[] data;

  public PrimordialEntityOutput() {
    this(new int[IntMath.divide(DATA_SIZE_BITS, Integer.SIZE, RoundingMode.CEILING)]);

    setMoveDirection(MoveDirection.NONE);
    setTurnDirection(TurnDirection.NONE);
  }

  public PrimordialEntityOutput(int[] data) {
    this.data = data;
  }

  public MoveDirection getMoveDirection() {
    int moveDirectionOrdinal = getBits(0, MOVE_DIRECTION_IDX, 1);
    moveDirectionOrdinal = moveDirectionOrdinal < MOVE_DIRECTIONS.length ? moveDirectionOrdinal : 0;
    return MOVE_DIRECTIONS[moveDirectionOrdinal];
  }

  public void setMoveDirection(MoveDirection moveDirection) {
    if (moveDirection == MoveDirection.BACKWARD) {
      throw new IllegalArgumentException(MoveDirection.BACKWARD.name() + " is not supported");
    }
    setBits(0, MOVE_DIRECTION_IDX, 1, moveDirection.ordinal());
  }

  public TurnDirection getTurnDirection() {
    int turnDirectionOrdinal = getBits(0, TURN_DIRECTION_IDX, 2);
    turnDirectionOrdinal = turnDirectionOrdinal < TURN_DIRECTIONS.length ? turnDirectionOrdinal : 0;
    return TURN_DIRECTIONS[turnDirectionOrdinal];
  }

  public void setTurnDirection(TurnDirection turnDirection) {
    setBits(0, TURN_DIRECTION_IDX, 2, turnDirection.ordinal());
  }

  public boolean shouldConsume() {
    return getBits(0, CONSUME_IDX, 1) == 1;
  }

  public void setConsume(boolean shouldConsume) {
    setBits(0, CONSUME_IDX, 1, shouldConsume ? 1 : 0);
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

  private int getBits(int idx, int bitOffset, int bitCount) {
    int mask = (1 << bitCount) - 1;
    return data[idx] >>> bitOffset & mask;
  }

  private void setBits(int idx, int bitOffset, int bitCount, int value) {
    int srcMask = (1 << bitCount) - 1;
    int destMask = ~(srcMask << bitOffset);
    data[idx] = (data[idx] & destMask) | ((value & srcMask) << bitOffset);
  }
}
