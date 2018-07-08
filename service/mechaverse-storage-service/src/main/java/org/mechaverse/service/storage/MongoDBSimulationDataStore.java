package org.mechaverse.service.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.mechaverse.simulation.common.datastore.AbstractSimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStoreInputStream;

import java.util.function.Supplier;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

/**
 * A {@link SimulationDataStore} implementation that utilizes MongoDB.
 *
 * For a given store, at least one document will be created that includes the simulation Id,
 * instance Id, and iteration. This is to allow for a store that contains no keys. As keys are
 * added, additional documents will be added that contain the simulation Id, instance Id, iteration,
 * key, and value. A unique index exists for simulation Id, instance Id, iteration, and key, which
 * not only provides performance optimizations, but all ensure that duplicate entries are not
 * allowed.
 *
 * Useful queries when working with the MongoDB database directly:
 *
 * <pre>
 * Find all unique stores:
 * > db.SimulationDataStores.find({"key": {$exists: false}})
 *
 * Find all keys for given simulation Id, instance Id, and iteration:
 * > db.SimulationDataStores.find({"simulationId" : "0249baa9-f38f-4435-add8-da988d217fbb",
 *                                 "instanceId" : "719912d5-809b-462f-a9c4-b9748d5a8f95",
 *                                 "iteration" : 0,
 *                                 "key": {$exists: true}})
 *
 * Find all keys with a given prefix for given simulation Id, instance Id, and iteration:
 * > db.SimulationDataStores.find({"simulationId" : "0249baa9-f38f-4435-add8-da988d217fbb",
 *                                 "instanceId" : "719912d5-809b-462f-a9c4-b9748d5a8f95",
 *                                 "iteration" : 100,
 *                                 "key": {$regex: "entity"}}
 * </pre>
 *
 *
 *
 * @author Dusty Hendrickson (dhendrickson@mechaverse.org)
 */
public class MongoDBSimulationDataStore extends AbstractSimulationDataStore {

  // Constants
  private static final String mongoCollectionName = "SimulationDataStores";
  private static final String simulationIdKey = "simulationId";
  private static final String instanceIdKey = "instanceId";
  private static final String iterationKey = "iteration";
  private static final String keyKey = "key";
  private static final String valueKey = "value";

  private DB mongoDatabase;
  private String simulationId;
  private String instanceId;
  private long iteration;

  /**
   * A {@link SimulationDataStoreInputStream} implementation utilizing
   * {@link MongoDBSimulationDataStore}.
   */
  public static final class MongoDBSimulationDataStoreInputStream
      extends SimulationDataStoreInputStream {

    /**
     * Generates a supplier for the given database and state parameters. This process will delete an
     * existing store and recreate it.
     */
    private static Supplier<SimulationDataStore> getSupplier(final DB mongoDatabase,
        final String simulationId, final String instanceId, long iteration) throws IOException {
      MongoDBSimulationDataStore.delete(mongoDatabase, simulationId, instanceId, iteration);
      MongoDBSimulationDataStore.create(mongoDatabase, simulationId, instanceId, iteration);
      final SimulationDataStore store =
          new MongoDBSimulationDataStore(mongoDatabase, simulationId, instanceId, iteration);

      return () -> store;
    }

    public MongoDBSimulationDataStoreInputStream(InputStream in, final DB mongoDatabase,
        final String simulationId, final String instanceId, final long iteration)
        throws IOException {
      super(in, getSupplier(mongoDatabase, simulationId, instanceId, iteration));
    }
  }

  /**
   * Creates a MongoDB store for the given parameters.
   */
  public static void create(DB mongoDatabase, String simulationId, String instanceId, long iteration)
      throws IOException {
    // Ensure unique index exists
    DBObject indexFields = new BasicDBObject();
    indexFields.put(simulationIdKey, 1);
    indexFields.put(instanceIdKey, 1);
    indexFields.put(iterationKey, 1);
    indexFields.put(keyKey, 1);
    mongoDatabase.getCollection(mongoCollectionName).createIndex(indexFields,
        new BasicDBObject("unique", true));

    // Build basic document
    DBObject stateDocument = new BasicDBObject();
    stateDocument.put(simulationIdKey, simulationId);
    stateDocument.put(instanceIdKey, instanceId);
    stateDocument.put(iterationKey, iteration);

    try {
      mongoDatabase.getCollection(mongoCollectionName).insert(stateDocument);
    } catch (MongoException ex) {
      throw new IOException(String.format("unable to create state document for /%s/%s/%s",
          simulationId, instanceId, iteration), ex);
    }
  }

  /**
   * Deletes one or more MongoDB stores based on the given parameters.
   *
   * @param mongoDatabase MongoDB database connection
   * @param simulationId If not null, add simulation Id to query of stores to be deleted
   * @param instanceId If not null, add instance Id to query of stores to be deleted
   * @param iteration If not null, add iteration to query of stores to be deleted
   */
  public static void delete(DB mongoDatabase, String simulationId, String instanceId, Long iteration) throws IOException {
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

    try {
      mongoDatabase.getCollection(mongoCollectionName).remove(query);
    } catch(Exception ex) {
      throw new IOException(ex);
    }
  }

  /**
   * Instantiates a store object tied to the given database and store parameters. An exception will
   * be thrown if the store object does not exist in the database.
   */
  public MongoDBSimulationDataStore(DB mongoDatabase, String simulationId, String instanceId,
      long iteration) throws IOException {
    this.mongoDatabase = mongoDatabase;
    this.simulationId = simulationId;
    this.instanceId = instanceId;
    this.iteration = iteration;

    // Build query to find existing document
    DBObject query = new BasicDBObject();
    query.put(simulationIdKey, simulationId);
    query.put(instanceIdKey, instanceId);
    query.put(iterationKey, iteration);
    query.put(keyKey, new BasicDBObject("$exists", false));

    DBObject record = mongoDatabase.getCollection(mongoCollectionName).findOne(query);
    if (record == null) {
      throw new IOException(String.format("state does not exist for /%s/%s/%s", simulationId,
          instanceId, iteration));
    }
  }

  @Override
  public byte[] get(String key) {
    DBObject query = new BasicDBObject();
    query.put(simulationIdKey, this.simulationId);
    query.put(instanceIdKey, this.instanceId);
    query.put(iterationKey, this.iteration);
    query.put(keyKey, key);

    byte[] value = null;
    DBObject record = mongoDatabase.getCollection(mongoCollectionName).findOne(query);
    if (record != null) {
      value = (byte[]) record.get(valueKey);
    }

    return value;
  }

  @Override
  public void put(String key, byte[] value) {
    DBObject query = new BasicDBObject();
    query.put(simulationIdKey, this.simulationId);
    query.put(instanceIdKey, this.instanceId);
    query.put(iterationKey, this.iteration);
    query.put(keyKey, key);

    DBObject entry = new BasicDBObject();
    entry.put(simulationIdKey, this.simulationId);
    entry.put(instanceIdKey, this.instanceId);
    entry.put(iterationKey, this.iteration);
    entry.put(keyKey, key);
    entry.put(valueKey, value);

    mongoDatabase.getCollection(mongoCollectionName).update(query, entry, true, false);
  }

  @Override
  public void remove(String key) {
    DBObject query = new BasicDBObject();
    query.put(simulationIdKey, this.simulationId);
    query.put(instanceIdKey, this.instanceId);
    query.put(iterationKey, this.iteration);
    query.put(keyKey, key);

    mongoDatabase.getCollection(mongoCollectionName).remove(query);
  }

  @Override
  public void clear() {
    DBObject query = new BasicDBObject();
    query.put(simulationIdKey, this.simulationId);
    query.put(instanceIdKey, this.instanceId);
    query.put(iterationKey, this.iteration);
    query.put(keyKey, new BasicDBObject("$exists", true));

    mongoDatabase.getCollection(mongoCollectionName).remove(query);
  }

  @Override
  public boolean containsKey(String key) {
    DBObject query = new BasicDBObject();
    query.put(simulationIdKey, this.simulationId);
    query.put(instanceIdKey, this.instanceId);
    query.put(iterationKey, this.iteration);
    query.put(keyKey, key);

    return mongoDatabase.getCollection(mongoCollectionName).count(query) > 0;
  }

  @Override
  public Set<String> keySet() {
    DBObject query = new BasicDBObject();
    query.put(simulationIdKey, this.simulationId);
    query.put(instanceIdKey, this.instanceId);
    query.put(iterationKey, this.iteration);
    query.put(keyKey, new BasicDBObject("$exists", true));

    DBObject filter = new BasicDBObject();
    filter.put(keyKey, 1);

    Set<String> keySet = new HashSet<>();
    DBCursor cursor = mongoDatabase.getCollection(mongoCollectionName).find(query, filter);
    while (cursor.hasNext()) {
      DBObject entry = cursor.next();
      String key = (String) entry.get(keyKey);
      keySet.add(key);
    }

    return keySet;
  }

  @Override
  public int size() {
    DBObject query = new BasicDBObject();
    query.put(simulationIdKey, this.simulationId);
    query.put(instanceIdKey, this.instanceId);
    query.put(iterationKey, this.iteration);
    query.put(keyKey, new BasicDBObject("$exists", true));

    long count = mongoDatabase.getCollection(mongoCollectionName).getCount(query);

    return (int) count;
  }

  @Override
  public Set<String> keysWithPrefix(String prefix) {
    throw new UnsupportedOperationException();
  }
}
