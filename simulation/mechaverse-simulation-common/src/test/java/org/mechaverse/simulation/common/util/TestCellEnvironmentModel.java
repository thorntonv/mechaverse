package org.mechaverse.simulation.common.util;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCellEnvironmentModel;
import org.mechaverse.simulation.common.model.EntityModel;

@JsonTypeName("TestCellEnvironmentModel")
public class TestCellEnvironmentModel extends
    AbstractCellEnvironmentModel<EntityModel<TestEntityType>, TestEntityType, TestCellModel> {

  @Override
  protected TestCellModel[][] createCells() {
    return new TestCellModel[getHeight()][getWidth()];
  }

  @Override
  protected TestCellModel createCell(int row, int column) {
    return new TestCellModel(row, column);
  }

  @Override
  public TestEntityType[] getEntityTypes() {
    return TestEntityType.values();
  }
}