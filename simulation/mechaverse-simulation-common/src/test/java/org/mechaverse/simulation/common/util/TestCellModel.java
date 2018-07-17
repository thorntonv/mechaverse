package org.mechaverse.simulation.common.util;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCellModel;
import org.mechaverse.simulation.common.model.EntityModel;

@JsonTypeName("TestCellModel")
public class TestCellModel extends
    AbstractCellModel<EntityModel<TestEntityType>, TestEntityType> {

  public TestCellModel(int row, int column) {
    super(row, column);
  }
}
