package org.mechaverse.service.storage;

import java.io.IOException;
import java.util.Collections;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import com.google.common.collect.ImmutableSet;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;

import static org.junit.Assert.*;

/**
 * 
 * Unit tests for {@link MongoDBSimulationDataStore}.
 * 
 * @author Dusty Hendrickson (dhendrickson@mechaverse.org)
 */
// TODO(dhendrickson): reintegrate with AbstractSimulationDataStoreTest
@Ignore
public class MongoDBSimulationDataStoreTest {
  private SimulationDataStore dataStore;

  private static MongodExecutable mongodExecutable;
  private static MongodProcess mongodProcess;
  private static MongoClient mongoClient;
  private static DB mongoDatabase;
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

    mongoClient = new MongoClient(mongoHost, mongoPort);
    mongoDatabase = mongoClient.getDB(mongoDatabaseName);
  }

  @AfterClass
  public static void afterClass() {
    mongodProcess.stop();
    mongodExecutable.stop();
  }

  @Before
  public void before() throws IOException {
    MongoDBSimulationDataStore.delete(mongoDatabase, "simulation1", "instance1", 1L);
    MongoDBSimulationDataStore.create(mongoDatabase, "simulation1", "instance1", 1);
    dataStore = new MongoDBSimulationDataStore(mongoDatabase, "simulation1", "instance1", 1);
  }

  @After
  public void after() throws IOException {
    MongoDBSimulationDataStore.delete(mongoDatabase, "simulation1", "instance1", 1L);
  }

  @Test
  public void testPutGetA() {
    dataStore.put("foo.bar", "testValue".getBytes());
    assertArrayEquals("testValue".getBytes(), dataStore.get("foo.bar"));
  }

  @Test
  public void testGetA() {
    assertArrayEquals(null, dataStore.get("foo.bar"));
  }

  @Test
  public void testRemoveA() {
    dataStore.put("foo.bar", "testValue".getBytes());
    dataStore.remove("foo.bar");
    assertFalse(dataStore.containsKey("foo.bar"));
    assertEquals(0, dataStore.keySet().size());
    assertNull(dataStore.get("foo.bar"));
  }

  @Test
  public void testRemoveB() {
    dataStore.remove("foo.bar");
    assertFalse(dataStore.containsKey("foo.bar"));
    assertEquals(0, dataStore.keySet().size());
    assertNull(dataStore.get("foo.bar"));
  }

  @Test
  public void testClearA() {
    dataStore.put("foo.bar.1", "testValue".getBytes());
    dataStore.put("foo.bar.2", "testValue".getBytes());
    dataStore.put("foo.bar.3", "testValue".getBytes());

    dataStore.clear();
    assertEquals(0, dataStore.size());
    assertEquals(Collections.emptySet(), dataStore.keySet());
    assertNull(dataStore.get("foo.bar.1"));
    assertNull(dataStore.get("foo.bar.2"));
    assertNull(dataStore.get("foo.bar.3"));
  }

  @Test
  public void testClearB() {
    dataStore.clear();
    assertEquals(0, dataStore.size());
    assertEquals(Collections.emptySet(), dataStore.keySet());
  }

  @Test
  public void testKeySetA() {
    dataStore.put("foo.bar.1", "testValue".getBytes());
    dataStore.put("foo.bar.2", "testValue".getBytes());
    dataStore.put("foo.bar.3", "testValue".getBytes());

    assertEquals(ImmutableSet.of("foo.bar.1", "foo.bar.2", "foo.bar.3"), dataStore.keySet());
  }

  @Test
  public void testKeySetB() {
    assertEquals(Collections.emptySet(), dataStore.keySet());
  }

  @Test
  public void testSizeA() {
    dataStore.put("foo.bar.1", "testValue".getBytes());
    dataStore.put("foo.bar.2", "testValue".getBytes());
    dataStore.put("foo.bar.3", "testValue".getBytes());

    assertEquals(3, dataStore.size());
  }

  @Test
  public void testSizeB() {
    assertEquals(0, dataStore.size());
  }
}
