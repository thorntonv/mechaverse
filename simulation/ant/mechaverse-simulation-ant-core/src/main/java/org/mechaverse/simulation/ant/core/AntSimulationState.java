package org.mechaverse.simulation.ant.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.mechaverse.simulation.ant.core.util.AntSimulationModelUtil;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.common.SimulationState;
import org.mechaverse.simulation.common.datastore.MemorySimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.datastore.SimulationDataStoreView;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;

/**
 * The ant simulation state.
 */
public final class AntSimulationState extends SimulationState<SimulationModel> {

  public static final String MODEL_KEY = "model";
  public static final String ENTITY_DATA_ROOT_KEY = "entity";
  public static final String REPLAY_DATA_ROOT_KEY = "replay";

  public AntSimulationState() {
    super(new SimulationModel(), new MemorySimulationDataStore());

    // Add placeholder for the model. This will be serialized when requested.
    put(MODEL_KEY, new byte[0]);
  }

  public AntSimulationState(SimulationDataStore dataStore) throws IOException {
    super(AntSimulationModelUtil.deserialize(new GZIPInputStream(
        new ByteArrayInputStream(dataStore.get(MODEL_KEY)))), dataStore);

    // Add placeholder for the model. This will be serialized when requested.
    put(MODEL_KEY, new byte[0]);
  }

  @Override
  public String getId() {
    return model.getId();
  }

  @Override
  public long getIteration() {
    return model.getIteration();
  }

  @Override
  public byte[] get(String key) {
    if(key.equals(MODEL_KEY)) {
      try {
        put(MODEL_KEY, AntSimulationModelUtil.serialize(model));
      } catch (IOException ignored) {}
    }
    return super.get(key);
  }

  public SimulationDataStore getEntityDataStore(EntityModel entity) {
    return new SimulationDataStoreView(getEntityRootKey(entity), this);
  }

  public GeneticDataStore getEntityGeneticDataStore(EntityModel entity) {
    return new GeneticDataStore(
        new SimulationDataStoreView(GeneticDataStore.KEY, getEntityDataStore(entity)));
  }

  public SimulationDataStore getEntityReplayDataStore(EntityModel entity) {
    return new SimulationDataStoreView(
        REPLAY_DATA_ROOT_KEY + SimulationDataStore.KEY_SEPARATOR + getEntityRootKey(entity), this);
  }

  public SimulationDataStore getReplayDataStore() {
    return new SimulationDataStoreView(REPLAY_DATA_ROOT_KEY, this);
  }

  private String getEntityRootKey(EntityModel entity) {
    return ENTITY_DATA_ROOT_KEY + SimulationDataStore.KEY_SEPARATOR + entity.getId();
  }
}
