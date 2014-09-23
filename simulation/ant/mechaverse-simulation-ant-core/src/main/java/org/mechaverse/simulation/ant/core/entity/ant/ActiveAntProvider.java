package org.mechaverse.simulation.ant.core.entity.ant;

import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.core.ActiveEntity;
import org.mechaverse.simulation.ant.core.ActiveEntityProvider;
import org.mechaverse.simulation.ant.core.entity.ant.ActiveAnt.AntBehavior;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides {@link ActiveAnt} instances.
 */
public class ActiveAntProvider implements ActiveEntityProvider {

  @Autowired ObjectFactory<AntBehavior> behaviorFactory;

  @Override
  public ActiveEntity getActiveEntity(Entity entity) {
    return new ActiveAnt((Ant) entity, behaviorFactory.getObject());
  }
}
