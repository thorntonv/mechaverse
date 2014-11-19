package org.mechaverse.service.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.common.datastore.MemorySimulationDataStore;
import org.mechaverse.simulation.common.datastore.MemorySimulationDataStore.MemorySimulationDataStoreInputStream;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStoreInputStream;
import org.mechaverse.simulation.common.datastore.SimulationDataStoreOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

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

  // Constants
  private final String mongoDataCollectionName = "data";
  private final String mongoMetadataCollectionName = "metadata";
  private final String simulationIdKey = "simulationId";
  private final String instanceIdKey = "instanceId";
  private final String iterationKey = "iteration";
  private final String catalogKey = "catalog";
  private final String dataKey = "data";
  private final String idKey = "_id";

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

      // Create a unique index using the simulation, instance, and iteration as the key
      // Note: this is safe to run multiple times, as subsequent creations are ignored
      DBObject indexKeys = new BasicDBObject();
      indexKeys.put(simulationIdKey, 1);
      indexKeys.put(instanceIdKey, 1);
      indexKeys.put(iterationKey, 1);
      mongoDatabase.getCollection(mongoMetadataCollectionName).createIndex(indexKeys,
          new BasicDBObject("unique", true));
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

    // Build query to find existing document
    DBObject query = new BasicDBObject();
    query.put(simulationIdKey, simulationId);
    query.put(instanceIdKey, instanceId);
    query.put(iterationKey, iteration);

    // Retrieve individual document from collection
    DBObject record = mongoDatabase.getCollection(mongoMetadataCollectionName).findOne(query);
    if (record == null) {
      throw new IOException(String.format("unable to get state for /%s/%s/%s", simulationId,
          instanceId, iteration));
    }
    DBObject catalog = (DBObject) record.get(catalogKey);

    SimulationDataStore store = new MemorySimulationDataStore();
    Map<String, ObjectId> keys = new HashMap<String, ObjectId>();
    buildList(keys, new StringBuilder(), catalog);
    for (String key : keys.keySet()) {
      query = new BasicDBObject();
      query.put(idKey, keys.get(key));
      record = mongoDatabase.getCollection(mongoDataCollectionName).findOne(query);
      if (record == null) {
        throw new IOException(String.format("unable to get state for /%s/%s/%s", simulationId,
            instanceId, iteration));
      }
      store.put(key, (byte[]) record.get(dataKey));
    }

    InputStream state =
        new ByteArrayInputStream(SimulationDataStoreOutputStream.toByteArray(store));

    return state;
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

    // Delete any existing state
    deleteState(simulationId, instanceId, iteration);

    // Build basic document
    DBObject metadataDocument = new BasicDBObject();
    metadataDocument.put(simulationIdKey, simulationId);
    metadataDocument.put(instanceIdKey, instanceId);
    metadataDocument.put(iterationKey, iteration);

    // Decode state and add to document
    SimulationDataStoreInputStream storeStream =
        new MemorySimulationDataStoreInputStream(stateInput);
    SimulationDataStore store = storeStream.readDataStore();
    storeStream.close();
    DBObject catalog = new BasicDBObject();
    for (String storeKey : store.keySet()) {
      DBObject dataDocument = new BasicDBObject();
      dataDocument.put(dataKey, store.get(storeKey));
      try {
        mongoDatabase.getCollection(mongoDataCollectionName).insert(dataDocument);
      } catch (MongoException ex) {
        throw new IOException(String.format("unable to set state for /%s/%s/%s", simulationId,
            instanceId, iteration), ex);
      }
      buildTree(storeKey, catalog, (ObjectId) dataDocument.get(idKey));
    }
    metadataDocument.put(catalogKey, catalog);

    // Attempt to update an existing document with matching query, otherwise insert a new document
    try {
      mongoDatabase.getCollection(mongoMetadataCollectionName).insert(metadataDocument);
    } catch (MongoException ex) {
      throw new IOException(String.format("unable to set state for /%s/%s/%s", simulationId,
          instanceId, iteration), ex);
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
    deleteState(simulationId, null, null);
  }

  @Override
  public void deleteInstance(String simulationId, String instanceId) throws IOException {
    deleteState(simulationId, instanceId, null);
  }

  private void deleteState(String simulationId, String instanceId, Long iteration)
      throws IOException {
    ensureDatabaseSetup();

    // Build query to find all documents for the given simulation and instance
    DBObject query = new BasicDBObject();
    if (simulationId != null) {
      query.put(simulationIdKey, simulationId);
    }
    if (instanceId != null) {
      query.put(instanceIdKey, instanceId);
    }
    if (iteration != null) {
      query.put(iterationKey, iteration);
    }

    // Retrieve individual document from collection
    DBObject record = mongoDatabase.getCollection(mongoMetadataCollectionName).findOne(query);
    if (record != null) {
      DBObject catalog = (DBObject) record.get(catalogKey);

      Map<String, ObjectId> keys = new HashMap<String, ObjectId>();
      buildList(keys, new StringBuilder(), catalog);

      // Remove metadata document
      try {
        mongoDatabase.getCollection(mongoMetadataCollectionName).remove(query);
      } catch (MongoException ex) {
        throw new IOException(String.format("unable to delete instance %s:%s", simulationId,
            instanceId), ex);
      }

      // Remove data documents
      for (String key : keys.keySet()) {
        query = new BasicDBObject();
        query.put(idKey, (ObjectId) keys.get(key));

        try {
          mongoDatabase.getCollection(mongoDataCollectionName).remove(query);
        } catch (MongoException ex) {
          throw new IOException(String.format("unable to delete instance /%s/%s", simulationId,
              instanceId), ex);
        }
      }
    }
  }

  private DBObject buildTree(String key, DBObject node, ObjectId objectId) {
    String[] split = key.split(Pattern.quote("."), 2);

    if (split.length == 1) {
      node.put(split[0], objectId);
      return node;
    } else {
      DBObject child = (DBObject) node.get(split[0]);
      if (child == null) {
        child = new BasicDBObject();
      }
      node.put(split[0], buildTree(split[1], child, objectId));
    }

    return node;
  }

  private void buildList(Map<String, ObjectId> keys, StringBuilder keyBuilder, DBObject node) {
    for (String key : node.keySet()) {
      if (node.get(key) instanceof DBObject) {
        StringBuilder childKeyBuilder = new StringBuilder(keyBuilder);
        buildList(keys, childKeyBuilder.append(key).append("."), (DBObject) node.get(key));
      } else {
        StringBuilder childKeyBuilder = new StringBuilder(keyBuilder);
        childKeyBuilder.append(key);
        keys.put(childKeyBuilder.toString(), (ObjectId) node.get(key));
      }
    }
  }
}
