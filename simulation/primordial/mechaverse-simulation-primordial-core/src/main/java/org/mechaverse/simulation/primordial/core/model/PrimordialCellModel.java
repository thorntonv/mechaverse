package org.mechaverse.simulation.primordial.core.model;

import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCellModel;
import org.mechaverse.simulation.common.model.EntityModel;

public class PrimordialCellModel extends AbstractCellModel<EntityModel<EntityType>, EntityType> {

  public PrimordialCellModel(int row, int column) {
    super(row, column);
  }
}