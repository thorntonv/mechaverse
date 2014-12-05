package org.mechaverse.service.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mechaverse.simulation.common.datastore.MemorySimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStoreInputStream;
import org.mechaverse.simulation.common.datastore.SimulationDataStoreOutputStream;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;

/**
 * 
 * Unit tests for {@link MongoDBMechaverseStorageService}.
 * 
 * @author Dusty Hendrickson (dhendrickson@mechaverse.org)
 */
public class MongoDBMechaverseStorageServiceTest {
  // TODO(dhendrickson): move some of this to an interface test

  private MongoDBMechaverseStorageService service;
  private static MongodExecutable mongodExecutable;
  private static MongodProcess mongodProcess;
  private static int mongoPort = 27017;
  private static String mongoHost = "127.0.0.1";
  private static String mongoDatabaseName = "mechaverse-storage-test";

  @BeforeClass
  public static void beforeClass() throws IOException {
    int port = 37017;
    IMongodConfig mongodConfig =
        new MongodConfigBuilder().version(Version.Main.PRODUCTION).net(new Net(port, false))
            .build();

    MongodStarter starter = MongodStarter.getDefaultInstance();
    mongodExecutable = starter.prepare(mongodConfig);
    mongodProcess = mongodExecutable.start();
  }

  @AfterClass
  public static void afterClass() {
    mongodProcess.stop();
    mongodExecutable.stop();
  }

  @Before
  public void before() throws IOException {
    this.service = new MongoDBMechaverseStorageService();
    this.service.setMongoPort(mongoPort);
    this.service.setMongoHost(mongoHost);
    this.service.setMongoDatabaseName(mongoDatabaseName);
    this.service.clear();
  }

  @After
  public void after() throws IOException {
    this.service.clear();
  }

  /**
   * Test setter/getter for MongoDB host.
   */
  @Test
  public void testMongoHost() {
    String setHost = "fake-host";
    this.service.setMongoHost(setHost);
    String getHost = this.service.getMongoHost();

    assertEquals(setHost, getHost);
  }

  /**
   * Test setter/getter for MongoDB port.
   */
  @Test
  public void testMongoPort() {
    int setPort = 1337;
    this.service.setMongoPort(setPort);
    int getPort = this.service.getMongoPort();

    assertEquals(setPort, getPort);
  }

  /**
   * Test setter/getter for MongoDB database name.
   */
  @Test
  public void testMongoDatabaseName() {
    String setDatabaseName = "fake-database";
    this.service.setMongoDatabaseName(setDatabaseName);
    String getDatabaseName = this.service.getMongoDatabaseName();

    assertEquals(setDatabaseName, getDatabaseName);
  }

  /**
   * Test setter/getter for state.
   * 
   * 1) Database is empty 2) Set state 3) Get state 4) Confirm equality of set and get
   */
  @Test
  public void testStateA() throws IOException {
    SimulationDataStore setStore = new MemorySimulationDataStore();
    setStore.put("a", "value1".getBytes());
    setStore.put("b.a", "value2".getBytes());
    setStore.put("b.b", "value3".getBytes());
    setStore.put("b.c.a", "value4".getBytes());

    InputStream setStream =
        new ByteArrayInputStream(SimulationDataStoreOutputStream.toByteArray(setStore));
    this.service.setState("simulation-id", "instance-id", 0, setStream);

    InputStream getStream;
    getStream = this.service.getState("simulation-id", "instance-id", 0);
    SimulationDataStoreInputStream tmpStream =
        new MemorySimulationDataStore.MemorySimulationDataStoreInputStream(getStream);
    SimulationDataStore getStore = tmpStream.readDataStore();
    tmpStream.close();

    assertEquals(setStore, getStore);
  }

  /*
   * /** Test setter/getter for state.
   * 
   * 1) Database is empty 2) Set state 3) Update state with same key set 4) Get state 5) Confirm
   * equality of update and get
   */
  @Test
  public void testStateB() throws IOException {
    SimulationDataStore setStore = new MemorySimulationDataStore();
    setStore.put("a", "value1".getBytes());
    setStore.put("b.a", "value2".getBytes());
    setStore.put("b.b", "value3".getBytes());
    setStore.put("b.c.a", "value4".getBytes());

    InputStream setStream =
        new ByteArrayInputStream(SimulationDataStoreOutputStream.toByteArray(setStore));
    this.service.setState("simulation-id", "instance-id", 0, setStream);

    SimulationDataStore updateStore = new MemorySimulationDataStore();
    updateStore.put("a", "value5".getBytes());
    updateStore.put("b.a", "value6".getBytes());
    updateStore.put("b.b", "value7".getBytes());
    updateStore.put("b.c.a", "value8".getBytes());

    InputStream updateStream =
        new ByteArrayInputStream(SimulationDataStoreOutputStream.toByteArray(updateStore));
    this.service.setState("simulation-id", "instance-id", 0, updateStream);

    InputStream getStream;
    getStream = this.service.getState("simulation-id", "instance-id", 0);
    SimulationDataStoreInputStream tmpStream =
        new MemorySimulationDataStore.MemorySimulationDataStoreInputStream(getStream);
    SimulationDataStore getStore = tmpStream.readDataStore();
    tmpStream.close();

    assertEquals(updateStore, getStore);
  }

  /**
   * Test setter/getter for state.
   * 
   * 1) Database is empty 2) Set state 3) Update state with different key set 4) Get state 5)
   * Confirm equality of update and get
   */
  @Test
  public void testStateC() throws IOException {
    SimulationDataStore setStore = new MemorySimulationDataStore();
    setStore.put("a", "value1".getBytes());
    setStore.put("b.a", "value2".getBytes());
    setStore.put("b.b", "value3".getBytes());
    setStore.put("b.c.a", "value4".getBytes());

    InputStream setStream =
        new ByteArrayInputStream(SimulationDataStoreOutputStream.toByteArray(setStore));
    this.service.setState("simulation-id", "instance-id", 0, setStream);

    SimulationDataStore updateStore = new MemorySimulationDataStore();
    updateStore.put("d", "value5".getBytes());
    updateStore.put("e.f", "value6".getBytes());
    updateStore.put("e.g", "value7".getBytes());
    updateStore.put("e.h.i", "value8".getBytes());

    InputStream updateStream =
        new ByteArrayInputStream(SimulationDataStoreOutputStream.toByteArray(updateStore));
    this.service.setState("simulation-id", "instance-id", 0, updateStream);

    InputStream getStream;
    getStream = this.service.getState("simulation-id", "instance-id", 0);
    SimulationDataStoreInputStream tmpStream =
        new MemorySimulationDataStore.MemorySimulationDataStoreInputStream(getStream);
    SimulationDataStore getStore = tmpStream.readDataStore();
    tmpStream.close();

    assertEquals(updateStore, getStore);
  }

  /**
   * Test setter/getter for state.
   * 
   * 1) Database is empty 2) Set state 3) Update state with empty key set 4) Get state 5) Confirm
   * equality of update and get
   */
  @Test
  public void testStateD() throws IOException {
    SimulationDataStore setStore = new MemorySimulationDataStore();
    setStore.put("key1", "value1".getBytes());
    setStore.put("key2", "value2".getBytes());
    setStore.put("key3", "value3".getBytes());

    InputStream setStream =
        new ByteArrayInputStream(SimulationDataStoreOutputStream.toByteArray(setStore));
    this.service.setState("simulation-id", "instance-id", 0, setStream);

    SimulationDataStore updateStore = new MemorySimulationDataStore();

    InputStream updateStream =
        new ByteArrayInputStream(SimulationDataStoreOutputStream.toByteArray(updateStore));
    this.service.setState("simulation-id", "instance-id", 0, updateStream);

    InputStream getStream;
    getStream = this.service.getState("simulation-id", "instance-id", 0);
    SimulationDataStoreInputStream tmpStream =
        new MemorySimulationDataStore.MemorySimulationDataStoreInputStream(getStream);
    SimulationDataStore getStore = tmpStream.readDataStore();
    tmpStream.close();

    assertEquals(updateStore, getStore);
  }

  /**
   * Test setter/getter for state.
   * 
   * 1) Database is empty 2) Set state with invalid stream 3) Confirm expected exception
   */
  @Test
  public void testStateE() {
    InputStream stream = new ByteArrayInputStream(new byte[0]);
    try {
      this.service.setState("simulation-id", "instance-id", 0, stream);
      fail("set state succeeded when should have failed");
    } catch (IOException ex) {
      // Expected condition
    }
  }

  /**
   * Test deleting a simulation.
   * 
   * 1) Database is empty 2) Set state for two simulation/instance/iteration 3) Delete one
   * simulation 4) Confirm only records for one simulation removed
   */
  @Test
  public void testDeleteSimulationA() throws IOException {
    SimulationDataStore store = new MemorySimulationDataStore();
    store.put("key1", "value1".getBytes());
    store.put("key2", "value2".getBytes());
    store.put("key3", "value3".getBytes());

    this.service.setState("simulation1", "instance1", 0, new ByteArrayInputStream(
        SimulationDataStoreOutputStream.toByteArray(store)));
    this.service.setState("simulation1", "instance1", 1, new ByteArrayInputStream(
        SimulationDataStoreOutputStream.toByteArray(store)));
    this.service.setState("simulation1", "instance2", 0, new ByteArrayInputStream(
        SimulationDataStoreOutputStream.toByteArray(store)));
    this.service.setState("simulation1", "instance2", 1, new ByteArrayInputStream(
        SimulationDataStoreOutputStream.toByteArray(store)));
    this.service.setState("simulation2", "instance1", 0, new ByteArrayInputStream(
        SimulationDataStoreOutputStream.toByteArray(store)));
    this.service.setState("simulation2", "instance1", 1, new ByteArrayInputStream(
        SimulationDataStoreOutputStream.toByteArray(store)));
    this.service.setState("simulation2", "instance2", 0, new ByteArrayInputStream(
        SimulationDataStoreOutputStream.toByteArray(store)));
    this.service.setState("simulation2", "instance2", 1, new ByteArrayInputStream(
        SimulationDataStoreOutputStream.toByteArray(store)));

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

  /**
   * Test deleting a simulation.
   * 
   * 1) Database is empty 2) Delete non-existent simulation 3) No exceptions are thrown
   */
  @Test
  public void testDeleteSimulationB() {
    try {
      this.service.deleteSimulation("fake-simulation");
    } catch (IOException e) {
      fail("delete simulation threw an unexpected exception");
    }
  }

  /**
   * Test deleting an instance simulation.
   */
  @Test
  public void testDeleteInstance() throws IOException {
    SimulationDataStore store = new MemorySimulationDataStore();
    store.put("key1", "value1".getBytes());
    store.put("key2", "value2".getBytes());
    store.put("key3", "value3".getBytes());

    this.service.setState("simulation1", "instance1", 0, new ByteArrayInputStream(
        SimulationDataStoreOutputStream.toByteArray(store)));
    this.service.setState("simulation1", "instance1", 1, new ByteArrayInputStream(
        SimulationDataStoreOutputStream.toByteArray(store)));
    this.service.setState("simulation1", "instance2", 0, new ByteArrayInputStream(
        SimulationDataStoreOutputStream.toByteArray(store)));
    this.service.setState("simulation1", "instance2", 1, new ByteArrayInputStream(
        SimulationDataStoreOutputStream.toByteArray(store)));
    this.service.setState("simulation2", "instance1", 0, new ByteArrayInputStream(
        SimulationDataStoreOutputStream.toByteArray(store)));
    this.service.setState("simulation2", "instance1", 1, new ByteArrayInputStream(
        SimulationDataStoreOutputStream.toByteArray(store)));
    this.service.setState("simulation2", "instance2", 0, new ByteArrayInputStream(
        SimulationDataStoreOutputStream.toByteArray(store)));
    this.service.setState("simulation2", "instance2", 1, new ByteArrayInputStream(
        SimulationDataStoreOutputStream.toByteArray(store)));

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
