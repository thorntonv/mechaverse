package org.mechaverse.gwt.client.util;

import org.mechaverse.api.model.simulation.ant.Ant;
import org.mechaverse.api.model.simulation.ant.Barrier;
import org.mechaverse.api.model.simulation.ant.Conduit;
import org.mechaverse.api.model.simulation.ant.Dirt;
import org.mechaverse.api.model.simulation.ant.Entity;
import org.mechaverse.api.model.simulation.ant.EntityType;
import org.mechaverse.api.model.simulation.ant.Food;
import org.mechaverse.api.model.simulation.ant.Pheromone;
import org.mechaverse.api.model.simulation.ant.Rock;

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
    } else if (entity instanceof Pheromone) {
      return EntityType.PHEROMONE;
    } else if (entity instanceof Rock) {
      return EntityType.ROCK;
    }
    return null;
  }
}
