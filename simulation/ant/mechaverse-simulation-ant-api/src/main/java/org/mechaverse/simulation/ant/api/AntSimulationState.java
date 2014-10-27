package org.mechaverse.simulation.ant.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.SimulationModel;
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
    super(deserializeModel(new GZIPInputStream(
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
        put(MODEL_KEY, serializeModel());
      } catch (IOException e) {}
    }
    return super.get(key);
  }

  public SimulationDataStore getEntityDataStore(Entity entity) {
    return new SimulationDataStoreView(getEntityRootKey(entity), this);
  }

  public GeneticDataStore getEntityGeneticDataStore(Entity entity) {
    return new GeneticDataStore(
        new SimulationDataStoreView(GeneticDataStore.KEY, getEntityDataStore(entity)));
  }

  public SimulationDataStore getEntityReplayDataStore(Entity entity) {
    return new SimulationDataStoreView(
        REPLAY_DATA_ROOT_KEY + SimulationDataStore.KEY_SEPARATOR + getEntityRootKey(entity), this);
  }

  public static void serializeModel(SimulationModel model, OutputStream out) throws IOException {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(SimulationModel.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.marshal(model, out);
    } catch (JAXBException e) {
      throw new IOException(e);
    }
  }

  public static SimulationModel deserializeModel(InputStream in) throws IOException {
    if (in == null) {
      return new SimulationModel();
    }
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(SimulationModel.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (SimulationModel) jaxbUnmarshaller.unmarshal(in);
    } catch (JAXBException e) {
      throw new IOException(e);
    }
  }

  private String getEntityRootKey(Entity entity) {
    return ENTITY_DATA_ROOT_KEY + SimulationDataStore.KEY_SEPARATOR + entity.getId();
  }

  private byte[] serializeModel() throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try (OutputStream out = new GZIPOutputStream(byteOut)) {
      serializeModel(model, out);
    }
    return byteOut.toByteArray();
  }
}
