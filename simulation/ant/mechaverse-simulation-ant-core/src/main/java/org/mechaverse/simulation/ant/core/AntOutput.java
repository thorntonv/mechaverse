package org.mechaverse.simulation.ant.core;

/**
 * Encodes and decodes the output of an ant.
 */
public final class AntOutput {

  public static enum MoveDirection {
    NONE, FORWARD, BACKWARD
  }

  public static enum TurnDirection {
    NONE, CLOCKWISE, COUNTERCLOCKWISE
  }

  public static final MoveDirection[] MOVE_DIRECTIONS = MoveDirection.values();
  public static final TurnDirection[] TURN_DIRECTIONS = TurnDirection.values();


  public static final int DATA_SIZE = 1;

  // TODO(thorntonv) Implement general output.
  // TODO(thorntonv) Implement interaction output.

  private static final int MOVE_DIRECTION_IDX = 0;
  private static final int MOVE_DIRECTION_MASK = ~0b11;

  private static final int TURN_DIRECTION_IDX = 0;
  private static final int TURN_DIRECTION_BIT_IDX = 2;
  private static final int TURN_DIRECTION_MASK = ~(0b11 << TURN_DIRECTION_BIT_IDX);

  private static final int PICKUP_IDX = 0;
  private static final int PICKUP_BIT_IDX = 4;
  private static final int PICKUP_MASK = ~(0b1 << PICKUP_BIT_IDX);

  private static final int DROP_IDX = 0;
  private static final int DROP_BIT_IDX = 5;
  private static final int DROP_MASK = ~(0b1 << DROP_BIT_IDX);

  private static final int LEAVE_PHEROMONE_IDX = 0;
  private static final int LEAVE_PHEROMONE_BIT_IDX = 6;
  private static final int LEAVE_PHEROMONE_MASK = 0b1111 << LEAVE_PHEROMONE_BIT_IDX;

  private final int[] data;

  public AntOutput() {
    this(new int[DATA_SIZE]);

    setMoveDirection(MoveDirection.NONE);
    setTurnDirection(TurnDirection.NONE);
    setPickUp(false);
    setDrop(false);
    setLeavePheromone(0);
  }

  public AntOutput(int[] data) {
    this.data = data;
  }

  public MoveDirection getMoveDirection() {
    return MOVE_DIRECTIONS[(data[MOVE_DIRECTION_IDX] & ~MOVE_DIRECTION_MASK)];
  }

  public void setMoveDirection(MoveDirection moveDirection) {
    int value = moveDirection.ordinal();
    data[MOVE_DIRECTION_IDX] = (data[MOVE_DIRECTION_IDX] & MOVE_DIRECTION_MASK) | value;
  }

  public TurnDirection getTurnDirection() {
    return TURN_DIRECTIONS[(data[TURN_DIRECTION_IDX] & ~TURN_DIRECTION_MASK) >> TURN_DIRECTION_BIT_IDX];
  }

  public void setTurnDirection(TurnDirection turnDirection) {
    int value = turnDirection.ordinal();
    data[TURN_DIRECTION_IDX] =
        (data[TURN_DIRECTION_IDX] & TURN_DIRECTION_MASK) | (value << TURN_DIRECTION_BIT_IDX);
  }

  public boolean pickUp() {
    int pickUp = (data[PICKUP_IDX] & ~PICKUP_MASK) >> PICKUP_BIT_IDX;
    return pickUp == 1;
  }

  public void setPickUp(boolean pickUp) {
    int value = pickUp ? 1 : 0;
    data[PICKUP_IDX] = (data[PICKUP_IDX] & PICKUP_MASK) | (value << PICKUP_BIT_IDX);
  }

  public boolean drop() {
    int drop = (data[DROP_IDX] & ~DROP_MASK) >> DROP_BIT_IDX;
    return drop == 1;
  }

  public void setDrop(boolean drop) {
    int value = drop ? 1 : 0;
    data[DROP_IDX] = (data[DROP_IDX] & DROP_MASK) | (value << DROP_BIT_IDX);
  }

  public int getLeavePheromone() {
    return (data[LEAVE_PHEROMONE_IDX] & LEAVE_PHEROMONE_MASK) >> LEAVE_PHEROMONE_BIT_IDX;
  }

  public void setLeavePheromone(int type) {
    data[LEAVE_PHEROMONE_IDX] =
        (data[LEAVE_PHEROMONE_IDX] & ~LEAVE_PHEROMONE_MASK) | (type << LEAVE_PHEROMONE_BIT_IDX);
  }

  public int[] getData() {
    return data;
  }
}
