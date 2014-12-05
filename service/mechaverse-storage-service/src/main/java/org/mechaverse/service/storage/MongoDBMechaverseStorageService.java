package org.mechaverse.service.storage;

import java.io.IOException;
import java.io.InputStream;

import org.mechaverse.service.storage.MongoDBSimulationDataStore.MongoDBInputStream;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStoreInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.mongodb.DB;
import com.mongodb.MongoClient;

/**
 * A storage service implementation that utilizes MongoDB.
 *
 * All state is represented within a GridFS filesystem. As an example, a simple state might have
 * simulationId 1, instanceId 2, iteration 3, and contain keys model, entity.1.key1, and
 * entity.1.key2. This would be represented in GridFS as the following files:
 * 
 * <pre>
 * /1/2/3/model
 * /1/2/3/entity/1/key1
 * /1/2/3/entity/1/key2
 * </pre>
 * 
 * Each file contains the binary data associated with the given key. GridFS utilizes two collections
 * within the database to represent the virtual filesystem. The collection fs.files will contain the
 * filesystem metadata (filename, timestamp, checksum, etc.). The collection fs.chunks contains the
 * actual binary data, which will be chunked into multiple pieces if necessary to ensure
 * compatibility with the MongoFS maximum document size. GridFS is implemented entirely by client
 * drivers. All data can be accessed by any client that is GridFS aware as well as by standard
 * MongoDB queries directly utilizing the standard GridFS collections.
 *
 * The mongofiles application can be used to easily browse the available data via a command line
 * shell:
 * 
 * <pre>
 * # mongofiles -d mechaverse list
 * connected to: 127.0.0.1
 * /1/2/3/model
 * /1/2/3/entity/1/key1
 * /1/2/3/entity/1/key2
 * 
 * # mongofiles -d mechaverse search /1/2/3/entity
 * connected to: 127.0.0.1
 * /1/2/3/entity/1/key1
 * /1/2/3/entity/1/key2
 * 
 * # mongofiles -d mechaverse get -l key1.bin /1/2/3/entity/1/key1
 * connected to: 127.0.0.1
 * done write to: model.bin
 * </pre>
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
   * Returns an instance of a MongoDB database.
   *
   * @param mongoHost hostname or IP address of the MongoDB server
   * @param mongoPort port of the MongoDB server
   * @param mongoDatabaseName database name on the MongoDB server
   * @return MongoDB database
   */
  private void ensureDatabaseSetup() throws IOException {
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
  protected void clear() throws IOException {
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

    return SimulationDataStoreInputStream.newInputStream(store);
  }

  @Override
  public InputStream getStateValue(String simulationId, String instanceId, long iteration,
      String key) throws IOException {
    logger.debug("Get state value for /{}/{}/{}/{}", simulationId, instanceId, iteration, key);
    throw new UnsupportedOperationException();
  }

  @Override
  public void setState(String simulationId, String instanceId, long iteration,
      InputStream stateInput) throws IOException {
    logger.debug("Set state for /{}/{}/{}", simulationId, instanceId, iteration);

    ensureDatabaseSetup();

    MongoDBInputStream stream = null;
    try {
      stream =
          new MongoDBInputStream(stateInput, mongoDatabase, simulationId, instanceId, iteration);
      stream.readDataStore();
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
  }

  @Override
  public void setStateValue(String simulationId, String instanceId, long iteration, String key,
      InputStream valueInput) throws IOException {
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
