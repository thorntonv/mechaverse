package org.mechaverse.simulation.primordial.core.model;

import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCellModel;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.entity.EntityUtil;

public class PrimordialCellModel extends AbstractCellModel<EntityModel, EntityType> {

  public PrimordialCellModel(int row, int column) {
    super(row, column);
  }

  @Override
  protected EntityType getType(EntityModel entity) {
    return EntityUtil.getType(entity);
  }
}
