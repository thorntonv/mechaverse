package org.mechaverse.service.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.common.SimulationDataStore;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.MongoClient;

// TODO(dhendrickson): move some of this to an interface test
// TODO(dhendrickson): add javadoc
// TODO(dhendrickson): look into embedded database to eliminate test dependency
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context.xml")
public class MongoDBMechaverseStorageServiceTest {

  private MongoDBMechaverseStorageService service;

  // TODO(dhendrickson): relocate this to SimulationDataStore and provide new hashCode()
  boolean compareSimulationDataStore(SimulationDataStore storeA, SimulationDataStore storeB) {
    if (storeA.size() != storeB.size()) {
      return false;
    }

    for (String key : storeA.keySet()) {
      if (!Arrays.equals(storeA.get(key), storeB.get(key))) {
        return false;
      }
    }

    return true;
  }

  // TODO(dhendrickson): figure out how to configure this better
  private void removeDatabase() throws IOException {
    MongoClient mongoClient = new MongoClient("localhost", 27017);
    mongoClient.getDB("mechaverse-storage-test").dropDatabase();
    mongoClient.close();
  }

  @Before
  public void before() throws IOException {
    removeDatabase();
    this.service = new MongoDBMechaverseStorageService();
  }

  @After
  public void after() throws IOException {
    removeDatabase();
  }

  @Test
  public void testMongoHost() {
    String setHost = "fake-host";
    this.service.setMongoHost(setHost);
    String getHost = this.service.getMongoHost();

    assertEquals(setHost, getHost);
  }

  @Test
  public void testMongoPort() {
    int setPort = 1337;
    this.service.setMongoPort(setPort);
    int getPort = this.service.getMongoPort();

    assertEquals(setPort, getPort);
  }

  @Test
  public void testMongoDatabaseName() {
    String setDatabaseName = "fake-database";
    this.service.setMongoDatabaseName(setDatabaseName);
    String getDatabaseName = this.service.getMongoDatabaseName();

    assertEquals(setDatabaseName, getDatabaseName);
  }

  @Ignore
  @Test
  public void testState() throws IOException {
    SimulationDataStore setStore = new SimulationDataStore();
    setStore.put("key1", "value1".getBytes());
    setStore.put("key2", "value2".getBytes());
    setStore.put("key3", "value3".getBytes());
    InputStream setStream = new ByteArrayInputStream(setStore.serialize());

    this.service.setState("simulation-id", "instance-id", 0, setStream);

    InputStream getStream;
    getStream = this.service.getState("simulation-id", "instance-id", 0);
    SimulationDataStore getStore = SimulationDataStore.deserialize(getStream);

    assertTrue(compareSimulationDataStore(setStore, getStore));
  }

  @Ignore
  @Test
  public void testDeleteSimulation() throws IOException {
    SimulationDataStore store = new SimulationDataStore();
    store.put("key1", "value1".getBytes());
    store.put("key2", "value2".getBytes());
    store.put("key3", "value3".getBytes());

    this.service.setState("simulation1", "instance1", 0,
        new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation1", "instance1", 1,
        new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation1", "instance2", 0,
        new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation1", "instance2", 1,
        new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation2", "instance1", 0,
        new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation2", "instance1", 1,
        new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation2", "instance2", 0,
        new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation2", "instance2", 1,
        new ByteArrayInputStream(store.serialize()));

    this.service.deleteSimulation("simulation2");

    assertTrue(this.service.getState("simulation1", "instance1", 0) instanceof InputStream);
    assertTrue(this.service.getState("simulation1", "instance1", 1) instanceof InputStream);
    assertTrue(this.service.getState("simulation1", "instance2", 0) instanceof InputStream);
    assertTrue(this.service.getState("simulation1", "instance2", 1) instanceof InputStream);

    try {
      this.service.getState("simulation2", "instance1", 0);
      fail("retrieved state that should not exist");
    } catch (IOException ex) {
      // Expected condition
    }

    try {
      this.service.getState("simulation2", "instance1", 1);
      fail("retrieved state that should not exist");
    } catch (IOException ex) {
      // Expected condition
    }

    try {
      this.service.getState("simulation2", "instance2", 0);
      fail("retrieved state that should not exist");
    } catch (IOException ex) {
      // Expected condition
    }

    try {
      this.service.getState("simulation2", "instance2", 1);
      fail("retrieved state that should not exist");
    } catch (IOException ex) {
      // Expected condition
    }
  }

  @Ignore
  @Test
  public void testDeleteInstance() throws IOException {
    SimulationDataStore store = new SimulationDataStore();
    store.put("key1", "value1".getBytes());
    store.put("key2", "value2".getBytes());
    store.put("key3", "value3".getBytes());

    this.service.setState("simulation1", "instance1", 0,
        new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation1", "instance1", 1,
        new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation1", "instance2", 0,
        new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation1", "instance2", 1,
        new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation2", "instance1", 0,
        new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation2", "instance1", 1,
        new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation2", "instance2", 0,
        new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation2", "instance2", 1,
        new ByteArrayInputStream(store.serialize()));

    this.service.deleteInstance("simulation2", "instance2");

    assertTrue(this.service.getState("simulation1", "instance1", 0) instanceof InputStream);
    assertTrue(this.service.getState("simulation1", "instance1", 1) instanceof InputStream);
    assertTrue(this.service.getState("simulation1", "instance2", 0) instanceof InputStream);
    assertTrue(this.service.getState("simulation1", "instance2", 1) instanceof InputStream);
    assertTrue(this.service.getState("simulation2", "instance1", 0) instanceof InputStream);
    assertTrue(this.service.getState("simulation2", "instance1", 1) instanceof InputStream);

    try {
      this.service.getState("simulation2", "instance2", 0);
      fail("retrieved state that should not exist");
    } catch (IOException ex) {
      // Expected condition
    }

    try {
      this.service.getState("simulation2", "instance2", 1);
      fail("retrieved state that should not exist");
    } catch (IOException ex) {
      // Expected condition
    }
  }
}
