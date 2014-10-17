package org.mechaverse.service.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mechaverse.simulation.common.SimulationDataStore;

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
 * @author Dusty Hendrickson <dhendrickson@mechaverse.org>
 */
public class MongoDBMechaverseStorageServiceTest {
  // TODO(dhendrickson): move some of this to an interface test

  private MongoDBMechaverseStorageService service;
  private static MongodExecutable mongodExecutable;
  private static MongodProcess mongodProcess;
  private static int mongoPort = 37017;
  private static String mongoHost = "127.0.0.1";
  private static String mongoDatabaseName = "mechaverse-storage-test";
  
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

  @BeforeClass
  public static void beforeClass() throws IOException
  {
    int port = 37017;
    IMongodConfig mongodConfig = new MongodConfigBuilder()
        .version(Version.Main.PRODUCTION)
        .net(new Net(port, false))
        .build();
    
    MongodStarter starter = MongodStarter.getDefaultInstance();
    mongodExecutable = starter.prepare(mongodConfig);
    mongodProcess = mongodExecutable.start();
  }
  
  @AfterClass
  public static void afterClass()
  {
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

  /**
   * Test setter/getter for state.
   * 
   * 1) Database is empty 2) Set state 3) Update state with same key set 4) Get state 5) Confirm
   * equality of update and get
   */
  @Test
  public void testStateB() throws IOException {
    SimulationDataStore setStore = new SimulationDataStore();
    setStore.put("key1", "value1".getBytes());
    setStore.put("key2", "value2".getBytes());
    setStore.put("key3", "value3".getBytes());

    InputStream setStream = new ByteArrayInputStream(setStore.serialize());
    this.service.setState("simulation-id", "instance-id", 0, setStream);

    SimulationDataStore updateStore = new SimulationDataStore();
    setStore.put("key1", "value4".getBytes());
    setStore.put("key2", "value5".getBytes());
    setStore.put("key3", "value6".getBytes());

    InputStream updateStream = new ByteArrayInputStream(updateStore.serialize());
    this.service.setState("simulation-id", "instance-id", 0, updateStream);

    InputStream getStream;
    getStream = this.service.getState("simulation-id", "instance-id", 0);
    SimulationDataStore getStore = SimulationDataStore.deserialize(getStream);

    assertTrue(compareSimulationDataStore(updateStore, getStore));
  }

  /**
   * Test setter/getter for state.
   * 
   * 1) Database is empty 2) Set state 3) Update state with different key set 4) Get state 5)
   * Confirm equality of update and get
   */
  @Test
  public void testStateC() throws IOException {
    SimulationDataStore setStore = new SimulationDataStore();
    setStore.put("key1", "value1".getBytes());
    setStore.put("key2", "value2".getBytes());
    setStore.put("key3", "value3".getBytes());

    InputStream setStream = new ByteArrayInputStream(setStore.serialize());
    this.service.setState("simulation-id", "instance-id", 0, setStream);

    SimulationDataStore updateStore = new SimulationDataStore();
    setStore.put("key4", "value4".getBytes());
    setStore.put("key5", "value5".getBytes());
    setStore.put("key6", "value6".getBytes());

    InputStream updateStream = new ByteArrayInputStream(updateStore.serialize());
    this.service.setState("simulation-id", "instance-id", 0, updateStream);

    InputStream getStream;
    getStream = this.service.getState("simulation-id", "instance-id", 0);
    SimulationDataStore getStore = SimulationDataStore.deserialize(getStream);

    assertTrue(compareSimulationDataStore(updateStore, getStore));
  }

  /**
   * Test setter/getter for state.
   * 
   * 1) Database is empty 2) Set state 3) Update state with empty key set 4) Get state 5) Confirm
   * equality of update and get
   */
  @Test
  public void testStateD() throws IOException {
    SimulationDataStore setStore = new SimulationDataStore();
    setStore.put("key1", "value1".getBytes());
    setStore.put("key2", "value2".getBytes());
    setStore.put("key3", "value3".getBytes());

    InputStream setStream = new ByteArrayInputStream(setStore.serialize());
    this.service.setState("simulation-id", "instance-id", 0, setStream);

    SimulationDataStore updateStore = new SimulationDataStore();

    InputStream updateStream = new ByteArrayInputStream(updateStore.serialize());
    this.service.setState("simulation-id", "instance-id", 0, updateStream);

    InputStream getStream;
    getStream = this.service.getState("simulation-id", "instance-id", 0);
    SimulationDataStore getStore = SimulationDataStore.deserialize(getStream);

    assertTrue(compareSimulationDataStore(updateStore, getStore));
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

  /**
   * Test deleting a simulation.
   * 
   * 1) Database is empty 2) Set state for two simulation/instance/iteration 3) Delete one
   * simulation 4) Confirm only records for one simulation removed
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

  /**
   * Test getter for state value.
   * 
   * 1) Database is empty 2) Set state 3) Get state value 4) Confirm state only contains requested
   * key
   */
  @Test
  public void testGetStateValue() throws IOException {
    SimulationDataStore store = new SimulationDataStore();
    store.put("key1", "value1".getBytes());
    store.put("key2", "value2".getBytes());
    store.put("key3", "value3".getBytes());

    this.service.setState("simulation1", "instance1", 0,
        new ByteArrayInputStream(store.serialize()));

    InputStream stream = this.service.getStateValue("simulation1", "instance1", 0, "key2");
    SimulationDataStore getStore = SimulationDataStore.deserialize(stream);

    assertEquals(1, getStore.size());
    assertTrue(getStore.containsKey("key2"));
    assertTrue(Arrays.equals(getStore.get("key2"), "value2".getBytes()));
  }
}
