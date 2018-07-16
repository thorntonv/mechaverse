package org.mechaverse.simulation.primordial.core.model;

import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCellEnvironmentModel;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.primordial.core.entity.EntityUtil;

public class PrimordialCellEnvironmentModel extends
    AbstractCellEnvironmentModel<EntityModel<EntityType>, EntityType, PrimordialCellModel> {

  @Override
  protected PrimordialCellModel[][] createCells() {
    return new PrimordialCellModel[getHeight()][getWidth()];
  }

  @Override
  protected PrimordialCellModel createCell(int row, int column) {
    return new PrimordialCellModel(row, column);
  }

  @Override
  public EntityType[] getEntityTypes() {
    return EntityUtil.ENTITY_TYPES;
  }
}
