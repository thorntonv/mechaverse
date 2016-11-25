package org.mechaverse.simulation.ant.core.entity.ant;

import org.mechaverse.simulation.ant.core.entity.ActiveEntity;
import org.mechaverse.simulation.ant.core.entity.ant.ActiveAnt.AntBehavior;
import org.mechaverse.simulation.ant.core.model.Ant;
import org.mechaverse.simulation.common.model.Entity;
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
