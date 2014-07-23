package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;

/**
 * An entity that is active in the environment.
 */
public interface ActiveEntity {

  void updateInput(CellEnvironment env);
  void performAction(CellEnvironment env);

  Entity getEntity();
  EntityType getType();
}
