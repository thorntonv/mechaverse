package org.mechaverse.simulation.ant.core.entity;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mechaverse.simulation.common.model.Entity;

/**
 * A {@link DataInputStream} for reading {@link Entity} instances.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class EntityDataInputStream extends DataInputStream {

  // TODO(thorntonv): Implement unit test for this class.
  
  public EntityDataInputStream(InputStream in) {
    super(in);
  }

  public Entity readEntity() throws IOException {
    Entity entity = EntityUtil.newEntity(EntityUtil.ENTITY_TYPES[readShort()]);
    entity.setId(readUTF());
    entity.setX(readInt());
    entity.setY(readInt());
    entity.setEnergy(readInt());
    entity.setMaxEnergy(readInt());
    entity.setDirection(EntityUtil.DIRECTIONS[readShort()]);
    return entity;
  }
}
