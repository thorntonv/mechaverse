package org.mechaverse.simulation.common.util;

import com.google.common.io.CharStreams;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCellEnvironmentModel;
import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCellModel;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.SimulationModel;

public class SimulationModelUtilTest {

  private enum TestEntityType {
    ENTITY
  }

  private static class TestCellModel extends AbstractCellModel<EntityModel, TestEntityType> {

    TestCellModel(int row, int column) {
      super(row, column);
    }

    @Override
    protected TestEntityType getType(EntityModel entity) {
      return TestEntityType.ENTITY;
    }
  }

  private static class TestCellEnvironmentModel extends
      AbstractCellEnvironmentModel<EntityModel, TestEntityType, TestCellModel> {

    @Override
    protected TestCellModel[][] createCells() {
      return new TestCellModel[getHeight()][getWidth()];
    }

    @Override
    protected TestCellModel createCell(int row, int column) {
      return new TestCellModel(row, column);
    }

    @Override
    protected TestEntityType[] getTypeValues() {
      return TestEntityType.values();
    }
  }

  @Test
  public void testSerialize_simulationModel() throws IOException {
    SimulationModel simulationModel = new SimulationModel();
    simulationModel.setId("abc");
    simulationModel.putData("key1", "testValue1".getBytes());

    Class[] classes = new Class[]{
        SimulationModel.class,
    };

    byte[] serialized = SimulationModelUtil.serialize(simulationModel, classes);

    SimulationModel deserializedModel = SimulationModelUtil.deserialize(new GZIPInputStream(new ByteArrayInputStream(serialized)), classes);
  }

  @Test
  public void testSerialize_cellEnvironmentModel() throws IOException {
    SimulationModel simulationModel = new SimulationModel();
    TestCellEnvironmentModel environmentModel = new TestCellEnvironmentModel();
    environmentModel.setWidth(10);
    environmentModel.setHeight(10);
    EntityModel entityModel = new EntityModel();
    entityModel.setId("e1");
    environmentModel.addEntity(entityModel, environmentModel.getCell(2, 3));
    simulationModel.setEnvironment(environmentModel);

    Class[] classes = new Class[]{
        SimulationModel.class,
        TestCellEnvironmentModel.class
    };
    byte[] serialized = SimulationModelUtil.serialize(simulationModel, classes);
//    assertEquals("", toString(serialized));

    SimulationModel deserializedModel = SimulationModelUtil.deserialize(new GZIPInputStream(new ByteArrayInputStream(serialized)), classes);
  }

  private String toString(byte[] serialized) throws IOException {
    return CharStreams.toString(new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(serialized))));
  }
}