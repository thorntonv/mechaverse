package org.mechaverse.service.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.cxf.helpers.IOUtils;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.common.SimulationDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

/**
 * A storage service implementation that utilizes a MongoDB database.
 *
 * A collection named simulationDataStores will be created in the desired database. Each document in
 * the collection will be structured as follows:
 *
 * {"simulationId": "b271815a-d81b-4398-8192-b15258bace42", "instanceId":
 * "e58e1866-ebf5-421b-a925-d70a09f0513a", "iteration": 0, "dataStoreEntries": {...} }
 *
 * A unique index will created for the collection that ensures that there is only one document for a
 * given simulationId, instanceId, and iteration combinations.
 *
 * @author Dusty Hendrickson (dhendrickson@mechaverse.org)
 */
public class MongoDBMechaverseStorageService implements MechaverseStorageService {
  private Logger logger = LoggerFactory.getLogger(MongoDBMechaverseStorageService.class);

  // Constants
  private final String mongoCollectionName = "simulationDataStores";
  private final String simulationIdKey = "simulationId";
  private final String instanceIdKey = "instanceId";
  private final String iterationKey = "iteration";
  private final String dataStoreKey = "dataStoreEntries";

  // Configuration
  @Value("${org.mechaverse.service.storage.mongoHost}")
  private String mongoHost = "localhost";

  @Value("${org.mechaverse.service.storage.mongoPort}")
  private int mongoPort = 27017;

  @Value("${org.mechaverse.service.storage.mongoDatabaseName}")
  private String mongoDatabaseName = "mechaverse-storage-service";

  // MongoDB client and database
  private MongoClient mongoClient;
  private DB mongoDatabase;

  /**
   * Returns an instance of a MongoDB database.
   *
   * @param mongoHost hostname or IP address of the MongoDB server
   * @param mongoPort port of the MongoDB server
   * @param mongoDatabaseName database name on the MongoDB server
   * @return MongoDB database
   */
  private DB getDatabase() throws IOException {
    // TODO(dhendrickson): look into thread safety issues of MongoDB driver
    if (mongoDatabase == null) {
      logger.debug("Creating MongoDB database connection to {}:{}/{}", mongoHost, mongoPort,
          mongoDatabaseName);

      mongoClient = new MongoClient(mongoHost, mongoPort);
      mongoDatabase = mongoClient.getDB(mongoDatabaseName);

      // Create a unique index using the simulation, instance, and iteration as the key
      // Note: this is safe to run multiple times, as subsequent creations are ignored
      DBObject indexKeys = new BasicDBObject();
      indexKeys.put(simulationIdKey, 1);
      indexKeys.put(instanceIdKey, 1);
      indexKeys.put(iterationKey, 1);
      mongoDatabase.getCollection(mongoCollectionName).createIndex(indexKeys,
          new BasicDBObject("unique", true));
    }

    return mongoDatabase;
  }

  /**
   * Removes the MongoDB database. Used primarily for testing purposes.
   */
  protected void clear() throws IOException {
    DB database = getDatabase();
    database.dropDatabase();
  }

  /**
   * Return the hostname or IP address of the MongoDB server.
   *
   * @return MongoDB hostname or IP address of the MongoDB server
   */
  public String getMongoHost() {
    return mongoHost;
  }

  /**
   * Set the hostname or IP address of the MongoDB server.
   *
   * @param mongoHost hostname or IP address of the MongoDB server
   */
  public void setMongoHost(String mongoHost) {
    this.mongoHost = mongoHost;
  }

  /**
   * Return the port of the MongoDB server.
   *
   * @return port of the MongoDB server
   */
  public int getMongoPort() {
    return mongoPort;
  }

  /**
   * Set the port of the MongoDB server.
   *
   * @param mongoPort port of the MongoDB server
   */
  public void setMongoPort(int mongoPort) {
    this.mongoPort = mongoPort;
  }

  /**
   * Return the database name on the MongoDB server.
   *
   * @return database name on the MongoDB server
   */
  public String getMongoDatabaseName() {
    return mongoDatabaseName;
  }

  /**
   * Set the database name on the MongoDB server.
   *
   * @param mongoDatabaseName database name on the MongoDB server
   */
  public void setMongoDatabaseName(String mongoDatabaseName) {
    this.mongoDatabaseName = mongoDatabaseName;
  }

  @Override
  public InputStream getState(String simulationId, String instanceId, long iteration)
      throws IOException {
    logger.debug("Get state for {}:{}:{}", simulationId, instanceId, iteration);

    // Setup database connection
    DB database = getDatabase();

    // Build query to find existing document
    DBObject query = new BasicDBObject();
    query.put(simulationIdKey, simulationId);
    query.put(instanceIdKey, instanceId);
    query.put(iterationKey, iteration);

    // Retrieve individual document from collection
    DBObject record = database.getCollection(mongoCollectionName).findOne(query);
    if (record == null) {
      throw new IOException(String.format("unable to get state for %s:%s:%s", simulationId,
          instanceId, iteration));
    }

    // Reconstruct state from document
    SimulationDataStore store = new SimulationDataStore();
    DBObject dataStoreEntries = (DBObject) record.get(dataStoreKey);
    for (String storeKey : dataStoreEntries.keySet()) {
      store.put(storeKey, (byte[]) dataStoreEntries.get(storeKey));
    }
    InputStream state = new ByteArrayInputStream(store.serialize());

    return state;
  }

  @Override
  public InputStream getStateValue(String simulationId, String instanceId, long iteration,
      String key) throws IOException {
    logger.debug("Get state value for {}:{}:{}:{}", simulationId, instanceId, iteration, key);
    throw new UnsupportedOperationException();
  }

  @Override
  public void setState(String simulationId, String instanceId, long iteration,
      InputStream stateInput) throws IOException {
    logger.debug("Set state for {}:{}:{}", simulationId, instanceId, iteration);

    // Setup database connection
    DB database = getDatabase();

    // Build basic document
    DBObject document = new BasicDBObject();
    document.put(simulationIdKey, simulationId);
    document.put(instanceIdKey, instanceId);
    document.put(iterationKey, iteration);

    // Decode state and add to document
    DBObject keys = new BasicDBObject();
    SimulationDataStore store =
        SimulationDataStore.deserialize(IOUtils.readBytesFromStream(stateInput));
    for (String storeKey : store.keySet()) {
      keys.put(storeKey, store.get(storeKey));
    }
    document.put(dataStoreKey, keys);

    // Build query to check for existing state document
    DBObject query = new BasicDBObject();
    query.put(simulationIdKey, simulationId);
    query.put(instanceIdKey, instanceId);
    query.put(iterationKey, iteration);

    // Attempt to update an existing document with matching query, otherwise insert a new document
    try {
      database.getCollection(mongoCollectionName).update(query, document, true, false);
    } catch (MongoException ex) {
      throw new IOException(String.format("unable to set state for %s:%s:%s", simulationId,
          instanceId, iteration), ex);
    }
  }

  @Override
  public void setStateValue(String simulationId, String instanceId, long iteration, String key,
      InputStream valueInput) throws IOException {
    logger.debug("Get state value for {}:{}:{}:{}", simulationId, instanceId, iteration, key);
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteSimulation(String simulationId) throws IOException {
    // Setup database connection
    DB database = getDatabase();

    // Build query to find all documents for the given simulation
    DBObject query = new BasicDBObject();
    query.put(simulationIdKey, simulationId);

    // Remove all documents that match query
    try {
      database.getCollection(mongoCollectionName).remove(query);
    } catch (MongoException ex) {
      throw new IOException(String.format("unable to delete simulation %s", simulationId), ex);
    }
  }

  @Override
  public void deleteInstance(String simulationId, String instanceId) throws IOException {
    // Setup database connection
    DB database = getDatabase();

    // Build query to find all documents for the given simulation and instance
    DBObject query = new BasicDBObject();
    query.put(simulationIdKey, simulationId);
    query.put(instanceIdKey, instanceId);

    // Remove all documents that match query
    try {
      database.getCollection(mongoCollectionName).remove(query);
    } catch (MongoException ex) {
      throw new IOException(String.format("unable to delete instance %s:%s", simulationId,
          instanceId), ex);
    }
  }
}
