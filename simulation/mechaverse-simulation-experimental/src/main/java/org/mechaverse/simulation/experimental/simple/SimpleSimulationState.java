package org.mechaverse.simulation.experimental.simple;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.mechaverse.simulation.common.SimulationState;
import org.mechaverse.simulation.common.datastore.MemorySimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStoreView;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;

import java.util.function.Function;

/**
 * The simple simulation state.
 */
public class SimpleSimulationState<M> extends SimulationState<M> {

  public static final String MODEL_KEY = "model";
  public static final String ENTITY_DATA_ROOT_KEY = "entity";

  private final Function<M, byte[]> modelSerializer;
  
  private long iteration = 0;
  
  public SimpleSimulationState(M model, Function<M, byte[]> modelSerializer) {
    super(model, new MemorySimulationDataStore());

    this.modelSerializer = modelSerializer;
    
    // Add placeholder for the model. This will be serialized when requested.
    put(MODEL_KEY, new byte[0]);
  }

  public SimpleSimulationState(Function<M, byte[]> modelSerializer, 
      Function<InputStream, M> modelDeserializer, SimulationDataStore dataStore) 
          throws IOException {
    super(modelDeserializer.apply(new GZIPInputStream(
        new ByteArrayInputStream(dataStore.get(MODEL_KEY)))), dataStore);

    this.modelSerializer = modelSerializer;
    
    // Add placeholder for the model. This will be serialized when requested.
    put(MODEL_KEY, new byte[0]);
  }

  @Override
  public String getId() {
    return new String(get("simulation-id"));
  }

  @Override
  public long getIteration() {
    return iteration;
  }
  
  public void setIteration(long iteration) {
    this.iteration = iteration;
  }

  @Override
  public byte[] get(String key) {
    if (key.equals(MODEL_KEY)) {
      put(MODEL_KEY, modelSerializer.apply(model));
    }
    return super.get(key);
  }

  public SimulationDataStore getEntityDataStore(SimpleCellularAutomatonEntity entity) {
    return new SimulationDataStoreView(getEntityRootKey(entity), this);
  }

  public GeneticDataStore getEntityGeneticDataStore(SimpleCellularAutomatonEntity entity) {
    return new GeneticDataStore(
        new SimulationDataStoreView(GeneticDataStore.KEY, getEntityDataStore(entity)));
  }

  private String getEntityRootKey(SimpleCellularAutomatonEntity entity) {
    return ENTITY_DATA_ROOT_KEY + SimulationDataStore.KEY_SEPARATOR + entity.getId();
  }
}
