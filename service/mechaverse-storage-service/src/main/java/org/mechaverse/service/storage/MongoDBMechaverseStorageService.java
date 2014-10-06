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

public class MongoDBMechaverseStorageService implements MechaverseStorageService {

  @Value("${org.mechaverse.service.storage.mongoHost}")
  private String mongoHost = "localhost";

  @Value("${org.mechaverse.service.storage.mongoPort}")
  private int mongoPort = 27017;

  @Value("${org.mechaverse.service.storage.mongoDatabase}")
  private String mongoDatabase = "mechaverse-storage-service";

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

  public String getMongoDatabase() {
    return mongoDatabase;
  }

  public void setMongoDatabase(String mongoDatabase) {
    this.mongoDatabase = mongoDatabase;
  }

  @Override
  public InputStream getState(String simulationId, String instanceId, long iteration)
      throws IOException {
    // Setup database connection
    MongoClient mongoClient = new MongoClient(this.mongoHost, this.mongoPort);
    DB database = mongoClient.getDB(this.mongoDatabase);

    // Construct database query
    BasicDBObject searchQuery = new BasicDBObject();
    searchQuery.put("simulationId", simulationId);
    searchQuery.put("instanceId", instanceId);
    searchQuery.put("iteration", iteration);

    // Retrieve individual record from database
    DBObject record = database.getCollection("states").findOne(searchQuery);
    if (record == null) {
      throw new IOException(String.format("unable to get state for %s:%s:%s", simulationId,
          instanceId, iteration));
    }

    // Reconstruct data
    SimulationDataStore store = new SimulationDataStore();
    DBObject keys = (DBObject) record.get("keys");
    for (String key : keys.keySet()) {
      store.put(key, (byte[]) keys.get(key));
    }

    return new ByteArrayInputStream(store.serialize());
  }

  @Override
  public InputStream getStateValue(String simulationId, String instanceId, long iteration,
      String key) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setState(String simulationId, String instanceId, long iteration,
      InputStream stateInput) throws IOException {
    // Setup database connection
    MongoClient mongoClient = new MongoClient(this.mongoHost, this.mongoPort);
    DB database = mongoClient.getDB(this.mongoDatabase);

    // Encode state into database
    SimulationDataStore store = SimulationDataStore.deserialize(stateInput);
    BasicDBObject entry = new BasicDBObject();
    BasicDBObject keys = new BasicDBObject();
    for (String key : store.keySet()) {
      keys.put(key, store.get(key));
    }
    entry.put("simulationId", simulationId);
    entry.put("instanceId", instanceId);
    entry.put("iteration", iteration);
    entry.put("keys", keys);
    database.getCollection("states").insert(entry);
  }

  @Override
  public void setStateValue(String simulationId, String instanceId, long iteration, String key,
      InputStream valueInput) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteSimulation(String simulationId) throws IOException {
    // Setup database connection
    MongoClient mongoClient = new MongoClient(this.mongoHost, this.mongoPort);
    DB database = mongoClient.getDB(this.mongoDatabase);
    
    // Remove all documents with given simulation ID
    BasicDBObject query = new BasicDBObject();
    query.append("simulationId", simulationId);
    database.getCollection("states").remove(query);    
  }

  @Override
  public void deleteInstance(String simulationId, String instanceId) throws IOException {
    // Setup database connection
    MongoClient mongoClient = new MongoClient(this.mongoHost, this.mongoPort);
    DB database = mongoClient.getDB(this.mongoDatabase);

    // Remove all documents with given simulation ID
    BasicDBObject query = new BasicDBObject();
    query.append("simulationId", simulationId);
    query.append("instanceId", instanceId);
    database.getCollection("states").remove(query); 
  }
}
