package org.mechaverse.simulation.common.util;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.IOException;
import org.junit.Test;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.SimulationModel;

public class SimulationModelUtilTest {

  @JsonTypeName("TestEntity1")
  public static class TestEntity1 extends EntityModel<TestEntityType> {

    private int field1;

    public int getField1() {
      return field1;
    }

    public void setField1(int field1) {
      this.field1 = field1;
    }

    @Override
    public TestEntityType getType() {
      return TestEntityType.ENTITY1;
    }
  }

  @JsonTypeName("TestEntity2")
  public static class TestEntity2 extends EntityModel<TestEntityType> {

    private String field2;

    public String getField2() {
      return field2;
    }

    public void setField2(String field2) {
      this.field2 = field2;
    }

    @Override
    public TestEntityType getType() {
      return TestEntityType.ENTITY2;
    }
  }

  @Test
  public void testSerializeDeserialize() throws IOException {
    SimulationModel simulationModel = new SimulationModel();
    simulationModel.setId("abc");
    simulationModel.putData("key1", "testValue1".getBytes());

    TestCellEnvironmentModel environmentModel = new TestCellEnvironmentModel();
    environmentModel.setWidth(10);
    environmentModel.setHeight(10);

    TestEntity1 entityModel1 = new TestEntity1();
    entityModel1.setId("e1");
    entityModel1.setField1(101);
    environmentModel.addEntity(entityModel1, environmentModel.getCell(2, 3));

    TestEntity2 entityModel2 = new TestEntity2();
    entityModel2.setId("e2");
    entityModel2.setField2("testStr");
    environmentModel.addEntity(entityModel2, environmentModel.getCell(5, 3));

    simulationModel.setEnvironment(environmentModel);

    byte[] serialized = SimulationModelUtil.serialize(simulationModel);

    SimulationModel deserializedModel = SimulationModelUtil.deserialize(serialized, new Class[]{
        TestCellEnvironmentModel.class, TestEntity1.class, TestEntity2.class}, SimulationModel.class);

    assertEquals("abc", deserializedModel.getId());
    assertEquals("testValue1", new String(deserializedModel.getData("key1")));
    environmentModel = (TestCellEnvironmentModel) deserializedModel.getEnvironment();
    assertEquals(10, environmentModel.getWidth());
    assertEquals(10, environmentModel.getHeight());
    assertEquals(2, environmentModel.getEntities().size());

    entityModel1 = (TestEntity1) environmentModel.getEntities().get(0);
    assertEquals("e1", entityModel1.getId());
    assertEquals(2, entityModel1.getY());
    assertEquals(101, entityModel1.getField1());
  }
}
