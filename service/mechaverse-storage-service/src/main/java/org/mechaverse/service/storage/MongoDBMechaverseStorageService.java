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

// TODO(dhendrickson): add javadoc
//
public class MongoDBMechaverseStorageService implements MechaverseStorageService {

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

  private static MongoClient mongoClient;
  private static DB mongoDatabase;

  private static DB getDatabase(String mongoHost, int mongoPort, String mongoDatabaseName)
      throws IOException {
    if (mongoClient == null) {
      mongoClient = new MongoClient(mongoHost, mongoPort);
      mongoDatabase = mongoClient.getDB(mongoDatabaseName);
    }

    return mongoDatabase;
  }

  public String getMongoHost() {
    return mongoHost;
  }

  public void setMongoHost(String mongoHost) {
    this.mongoHost = mongoHost;
  }

  public int getMongoPort() {
    return mongoPort;
  }

  public void setMongoPort(int mongoPort) {
    this.mongoPort = mongoPort;
  }

  public String getMongoDatabaseName() {
    return mongoDatabaseName;
  }

  public void setMongoDatabaseName(String mongoDatabaseName) {
    this.mongoDatabaseName = mongoDatabaseName;
  }

  @Override
  public InputStream getState(String simulationId, String instanceId, long iteration)
      throws IOException {
    // Setup database connection
    DB database = getDatabase(this.mongoHost, this.mongoPort, this.mongoDatabaseName);

    // Construct database query
    BasicDBObject searchQuery = new BasicDBObject();
    searchQuery.put(this.simulationIdKey, simulationId);
    searchQuery.put(this.instanceIdKey, instanceId);
    searchQuery.put(this.iterationKey, iteration);

    // Retrieve individual record from database
    DBObject record = database.getCollection(this.mongoCollectionName).findOne(searchQuery);
    if (record == null) {
      throw new IOException(String.format("unable to get state for %s:%s:%s", simulationId,
          instanceId, iteration));
    }

    // Reconstruct data
    SimulationDataStore store = new SimulationDataStore();
    DBObject keys = (DBObject) record.get(this.dataStoreKey);
    for (String key : keys.keySet()) {
      store.put(key, (byte[]) keys.get(key));
    }

    return new ByteArrayInputStream(store.serialize());
  }

  @Override
  public InputStream getStateValue(String simulationId, String instanceId, long iteration,
      String key) throws IOException {
    throw new UnsupportedOperationException();
    // TODO(dhendrickson): implement
  }

  @Override
  public void setState(String simulationId, String instanceId, long iteration,
      InputStream stateInput) throws IOException {
    // Setup database connection
    DB database = getDatabase(this.mongoHost, this.mongoPort, this.mongoDatabaseName);

    // Encode state into database
    // TODO(dhendrickson): deal with existing record
    SimulationDataStore store = SimulationDataStore.deserialize(stateInput);
    BasicDBObject entry = new BasicDBObject();
    BasicDBObject keys = new BasicDBObject();
    for (String key : store.keySet()) {
      keys.put(key, store.get(key));
    }
    entry.put(this.simulationIdKey, simulationId);
    entry.put(this.instanceIdKey, instanceId);
    entry.put(this.iterationKey, iteration);
    entry.put(this.dataStoreKey, keys);
    database.getCollection(this.mongoCollectionName).insert(entry);
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

    // Remove all documents with given simulation ID
    BasicDBObject query = new BasicDBObject();
    query.append(this.simulationIdKey, simulationId);
    database.getCollection(this.mongoCollectionName).remove(query);
    // TODO(dhendrickson): check error conditions
  }

  @Override
  public void deleteInstance(String simulationId, String instanceId) throws IOException {
    // Setup database connection
    DB database = getDatabase(this.mongoHost, this.mongoPort, this.mongoDatabaseName);

    // Remove all documents with given simulation ID
    BasicDBObject query = new BasicDBObject();
    query.append(this.simulationIdKey, simulationId);
    query.append(this.instanceIdKey, instanceId);
    database.getCollection(this.mongoCollectionName).remove(query);
    // TODO(dhendrickson): check error conditions
  }
}
