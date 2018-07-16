package org.mechaverse.simulation.ant.core.entity;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.mechaverse.simulation.common.model.EntityModel;

/**
 * A {@link DataOutputStream} for writing {@link EntityModel} instances.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class EntityDataOutputStream extends DataOutputStream {

  // TODO(thorntonv): Implement unit test for this class. 

  public EntityDataOutputStream(OutputStream out) {
    super(out);
  }

  public void writeEntity(EntityModel entity) throws IOException {
    writeShort(entity.getType().ordinal());
    writeUTF(entity.getId());
    writeInt(entity.getX());
    writeInt(entity.getY());
    writeInt(entity.getEnergy());
    writeInt(entity.getMaxEnergy());
    writeShort(entity.getDirection().ordinal());
  }
}
