package org.mechaverse.simulation.common;

import java.util.UUID;

/**
 * An entity base class.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public abstract class AbstractEntity {

  private final String id;

  public AbstractEntity() {
    this(UUID.randomUUID().toString());
  }

  public AbstractEntity(String id) {
    this.id = id;
  }

  public abstract int[] getInput();
  public abstract void processOutput(int[] output);

  public String getId() {
    return id;
  }
}
