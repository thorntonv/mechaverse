package org.mechaverse.service.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.common.SimulationDataStore;
import org.springframework.beans.factory.annotation.Value;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

/**
 * A storage service implementation that utilizes a MongoDB database.
 * 
 * @author Dusty Hendrickson <dusty@obsidiannight.com>
 */
public class MongoDBMechaverseStorageService implements MechaverseStorageService {

  // Constants
  private static final String mongoCollectionName = "simulationDataStores";
  private static final String simulationIdKey = "simulationId";
  private static final String instanceIdKey = "instanceId";
  private static final String iterationKey = "iteration";
  private static final String dataStoreKey = "dataStoreEntries";

  // Configuration
  @Value("${org.mechaverse.service.storage.mongoHost}")
  private String mongoHost = "localhost";

  @Value("${org.mechaverse.service.storage.mongoPort}")
  private int mongoPort = 27017;

  @Value("${org.mechaverse.service.storage.mongoDatabaseName}")
  private String mongoDatabaseName = "mechaverse-storage-service";

  // Singleton client and database
  private static MongoClient mongoClient;
  private static DB mongoDatabase;

  /**
   * Returns a singleton instance of a MongoDB database.
   * 
   * @param mongoHost hostname or IP address of the MongoDB server
   * @param mongoPort port of the MongoDB server
   * @param mongoDatabaseName database name on the MongoDB server
   * @return MongoDB database
   * @throws IOException
   */
  private static DB getDatabase(String mongoHost, int mongoPort, String mongoDatabaseName)
      throws IOException {
    if (mongoDatabase == null) {
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
   * 
   * @throws IOException
   */
  protected void clear() throws IOException {
    DB database = getDatabase(this.mongoHost, this.mongoPort, this.mongoDatabaseName);
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
    return this.getStateValue(simulationId, instanceId, iteration, null);
  }

  @Override
  public InputStream getStateValue(String simulationId, String instanceId, long iteration,
      String key) throws IOException {
    // Setup database connection
    DB database = getDatabase(this.mongoHost, this.mongoPort, this.mongoDatabaseName);

    // Build query to find existing document
    DBObject query = new BasicDBObject();
    query.put(simulationIdKey, simulationId);
    query.put(instanceIdKey, instanceId);
    query.put(iterationKey, iteration);

    // Filter fields by key if necessary
    DBObject fields = new BasicDBObject();
    if (key != null) {
      fields.put(String.format("%s.%s", dataStoreKey, key), 1);
    }

    // Retrieve individual document from collection
    DBObject record = database.getCollection(mongoCollectionName).findOne(query, fields);
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
  public void setState(String simulationId, String instanceId, long iteration,
      InputStream stateInput) throws IOException {
    // Setup database connection
    DB database = getDatabase(this.mongoHost, this.mongoPort, this.mongoDatabaseName);

    // Build basic document
    DBObject document = new BasicDBObject();
    document.put(simulationIdKey, simulationId);
    document.put(instanceIdKey, instanceId);
    document.put(iterationKey, iteration);

    // Decode state and add to document
    DBObject keys = new BasicDBObject();
    SimulationDataStore store = SimulationDataStore.deserialize(stateInput);
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
    throw new UnsupportedOperationException();
    // TODO(dhendrickson): implement
  }

  @Override
  public void deleteSimulation(String simulationId) throws IOException {
    // Setup database connection
    DB database = getDatabase(this.mongoHost, this.mongoPort, this.mongoDatabaseName);

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
    DB database = getDatabase(this.mongoHost, this.mongoPort, this.mongoDatabaseName);

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
