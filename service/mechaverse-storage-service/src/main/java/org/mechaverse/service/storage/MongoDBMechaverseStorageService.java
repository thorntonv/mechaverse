package org.mechaverse.service.storage;

import java.io.IOException;
import java.io.InputStream;

import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.common.SimulationDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.mongodb.DB;
import com.mongodb.MongoClient;

/**
 * A storage service implementation that utilizes MongoDB.
 *
 * 
 * 
 * @author Dusty Hendrickson (dhendrickson@mechaverse.org)
 */
public class MongoDBMechaverseStorageService implements MechaverseStorageService {
  private Logger logger = LoggerFactory.getLogger(MongoDBMechaverseStorageService.class);

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
   * Sets the instance of the MongoDB database.
   */
  private void ensureDatabaseSetup() {
    // TODO(dhendrickson): look into thread safety issues of MongoDB driver
    if (mongoDatabase == null) {
      logger.debug("Creating MongoDB database connection to {}:{}/{}", mongoHost, mongoPort,
          mongoDatabaseName);

      mongoClient = new MongoClient(mongoHost, mongoPort);
      mongoDatabase = mongoClient.getDB(mongoDatabaseName);
    }
  }

  /**
   * Removes the MongoDB database. Used primarily for testing purposes.
   */
  protected void clear() {
    ensureDatabaseSetup();
    mongoDatabase.dropDatabase();
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
    logger.debug("Get state for /{}/{}/{}", simulationId, instanceId, iteration);

    ensureDatabaseSetup();

    SimulationDataStore store;
    try {
      store = new MongoDBSimulationDataStore(mongoDatabase, simulationId, instanceId, iteration);
    } catch (IOException ex) {
      throw new IOException(String.format("state does not exist for /%s/%s/%d", simulationId,
          instanceId, iteration), ex);
    }

    // TODO(thorntonv): FIX !!!!!!
    return null;
  }

  @Override
  public InputStream getStateValue(String simulationId, String instanceId, long iteration,
      String key) {
    logger.debug("Get state value for /{}/{}/{}/{}", simulationId, instanceId, iteration, key);
    throw new UnsupportedOperationException();
  }

  @Override
  public void setState(String simulationId, String instanceId, long iteration,
      InputStream stateInput) throws IOException {
    logger.debug("Set state for /{}/{}/{}", simulationId, instanceId, iteration);

    ensureDatabaseSetup();

    // TODO(thorntonv): FIX ME !!!!!!
  }

  @Override
  public void setStateValue(String simulationId, String instanceId, long iteration, String key,
      InputStream valueInput) {
    logger.debug("Get state value for /{}/{}/{}/{}", simulationId, instanceId, iteration, key);
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteSimulation(String simulationId) throws IOException {
    ensureDatabaseSetup();
    MongoDBSimulationDataStore.delete(mongoDatabase, simulationId, null, null);
  }

  @Override
  public void deleteInstance(String simulationId, String instanceId) throws IOException {
    ensureDatabaseSetup();
    MongoDBSimulationDataStore.delete(mongoDatabase, simulationId, instanceId, null);
  }
}
