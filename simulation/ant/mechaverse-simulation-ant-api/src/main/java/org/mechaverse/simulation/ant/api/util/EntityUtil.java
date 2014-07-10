package org.mechaverse.simulation.ant.api.util;

import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Barrier;
import org.mechaverse.simulation.ant.api.model.Conduit;
import org.mechaverse.simulation.ant.api.model.Dirt;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Food;
import org.mechaverse.simulation.ant.api.model.Nest;
import org.mechaverse.simulation.ant.api.model.Pheromone;
import org.mechaverse.simulation.ant.api.model.Rock;

/**
 * Entity utility methods.
 *
 * @author thorntonv@mechaverse.org
 */
public class EntityUtil {

  public static EntityType getType(Entity entity) {
    if (entity instanceof Ant) {
      return EntityType.ANT;
    } else if (entity instanceof Barrier) {
      return EntityType.BARRIER;
    } else if (entity instanceof Conduit) {
      return EntityType.CONDUIT;
    } else if (entity instanceof Dirt) {
      return EntityType.DIRT;
    } else if (entity instanceof Food) {
      return EntityType.FOOD;
    } else if (entity instanceof Nest) {
      return EntityType.NEST;
    } else if (entity instanceof Pheromone) {
      return EntityType.PHEROMONE;
    } else if (entity instanceof Rock) {
      return EntityType.ROCK;
    }
    return null;
  }
}
