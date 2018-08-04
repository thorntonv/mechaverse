package org.mechaverse.simulation.common.util;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.io.CharStreams;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
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



  private static final String TEST_JSON = "{\"id\":\"abc\",\"environment\":{\"@type\":\"TestCellEnvironmentModel\",\"width\":10,\"height\":10,\"entities\":[{\"@type\":\"TestEntity1\",\"id\":\"e1\",\"x\":3,\"y\":2,\"energy\":0,\"maxEnergy\":0,\"age\":0,\"data\":{},\"field1\":101},{\"@type\":\"TestEntity2\",\"id\":\"e2\",\"x\":3,\"y\":5,\"energy\":0,\"maxEnergy\":0,\"age\":0,\"data\":{},\"field2\":\"testStr\"}]},\"subEnvironments\":[],\"iteration\":0,\"data\":{\"key1\":\"dGVzdFZhbHVlMQ==\"},\"persistEntityCellularAutomatonStateEnabled\":true}";

  @Test
  public void testSerialize() throws IOException {
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
    assertEquals(TEST_JSON, toString(serialized));
  }

  @Test
  public void testDeserialize() throws IOException {
    SimulationModel simulationModel = SimulationModelUtil.deserialize(toBytes(TEST_JSON), new Class[]{
        TestCellEnvironmentModel.class, TestEntity1.class, TestEntity2.class}, SimulationModel.class);

    assertEquals("abc", simulationModel.getId());
    assertEquals(2, simulationModel.getEnvironment().getEntities().size());
  }

  private String toString(byte[] serialized) throws IOException {
    return CharStreams.toString(new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(serialized))));
  }

  private byte[] toBytes(String jsonString) throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try(GZIPOutputStream out = new GZIPOutputStream(byteOut)) {
      out.write(jsonString.getBytes());
    }
    return byteOut.toByteArray();
  }
}