package org.mechaverse.simulation.ant.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.SimulationModel;
import org.mechaverse.simulation.common.SimulationDataStore;
import org.mechaverse.simulation.common.SimulationState;

/**
 * The ant simulation state.
 */
public final class AntSimulationState extends SimulationState<SimulationModel> {

  public static final String MODEL_KEY = "model";
  public static final String CONFIG_KEY = "config";

  private static final String ENTITY_KEY_PREFIX = "entity.";

  private AntSimulationConfig config = new AntSimulationConfig();

  public static AntSimulationState deserialize(byte[] data) throws IOException {
    return deserialize(new ByteArrayInputStream(data));
  }

  public static AntSimulationState deserialize(InputStream in) throws IOException {
    SimulationDataStore dataStore = SimulationDataStore.deserialize(in);
    return new AntSimulationState(dataStore);
  }

  public AntSimulationState() {
    super(new SimulationModel(), new SimulationDataStore());

    // Add placeholders for the model and config. These will be serialized from objects when
    // requested.
    put(MODEL_KEY, new byte[0]);
    put(CONFIG_KEY, new byte[0]);
  }

  public AntSimulationState(SimulationDataStore dataStore) throws IOException {
    super(deserializeModel(new GZIPInputStream(
        new ByteArrayInputStream(dataStore.get(MODEL_KEY)))), dataStore);
    this.config = AntSimulationConfig.deserialize(dataStore.get(CONFIG_KEY));

    // Add placeholders for the model and config. These will be serialized from objects when
    // requested.
    put(MODEL_KEY, new byte[0]);
    put(CONFIG_KEY, new byte[0]);
  }

  @Override
  public String getId() {
    return model.getId();
  }

  @Override
  public long getIteration() {
    return model.getIteration();
  }

  public AntSimulationConfig getConfig() {
    return config;
  }

  @Override
  public byte[] get(String key) {
    if(key.equals(MODEL_KEY)) {
      try {
        put(MODEL_KEY, serializeModel());
      } catch (IOException e) {}
    } else if(key.equals(CONFIG_KEY)) {
      try {
        put(CONFIG_KEY, config.serialize());
      } catch (IOException e) {}
    }
    return super.get(key);
  }

  @Override
  public byte[] serialize() throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    serialize(byteOut);
    return byteOut.toByteArray();
  }

  public byte[] getEntityValue(Entity entity, String key) {
    return get(getEntityKey(entity, key));
  }

  public void removeAllEntityValues() {
    Set<String> keys = new HashSet<>(keySet());
    for (String key : keys) {
      if (key.startsWith(ENTITY_KEY_PREFIX)) {
        remove(key);
      }
    }
  }

  public SimulationDataStore getEntityValues(Entity entity) {
    SimulationDataStore entityDataStore = new SimulationDataStore();
    String entityKeyPrefix = getEntityKeyPrefix(entity) + ".";
    for (String key : keySet()) {
      if (key.startsWith(entityKeyPrefix)) {
        byte[] value = get(key);
        String entityKey = key.substring(entityKeyPrefix.length());
        entityDataStore.put(entityKey, value);
      }
    }
    return entityDataStore;
  }

  public void putEntityValue(Entity entity, String key, byte[] value) {
    put(getEntityKey(entity, key), value);
  }

  public void putEntityValues(Entity entity, SimulationDataStore entityDataStore) {
    for (String key : entityDataStore.keySet()) {
      putEntityValue(entity, key, entityDataStore.get(key));
    }
  }

  @Override
  public void serialize(OutputStream out) throws IOException {
    put(MODEL_KEY, serializeModel());
    put(CONFIG_KEY, config.serialize());
    super.serialize(out);
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

  private byte[] serializeModel() throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try (OutputStream out = new GZIPOutputStream(byteOut)) {
      serializeModel(model, out);
    }
    return byteOut.toByteArray();
  }

  private String getEntityKeyPrefix(Entity entity) {
    return ENTITY_KEY_PREFIX + entity.getId();
  }

  private String getEntityKey(Entity entity, String key) {
    return getEntityKeyPrefix(entity) + "." + key;
  }
}
