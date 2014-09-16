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

import org.mechaverse.simulation.ant.api.model.SimulationModel;
import org.mechaverse.simulation.common.SimulationDataStore;
import org.mechaverse.simulation.common.SimulationState;

public class AntSimulationState extends SimulationState<SimulationModel> {

  public static final String MODEL_KEY = "model";

  public static AntSimulationState deserialize(byte[] data) throws IOException {
    return deserialize(new ByteArrayInputStream(data));
  }

  public static AntSimulationState deserialize(InputStream in) throws IOException {
    SimulationDataStore dataStore = SimulationDataStore.deserialize(in);
    return new AntSimulationState(dataStore);
  }

  public AntSimulationState() {
    super(new SimulationModel());
  }

  public AntSimulationState(SimulationDataStore dataStore) throws IOException {
    super(deserializeModel(new GZIPInputStream(
        new ByteArrayInputStream(dataStore.get(MODEL_KEY)))));
  }

  @Override
  public String getId() {
    return model.getId();
  }

  @Override
  public String getInstanceId() {
    return model.getEnvironment().getId();
  }

  @Override
  public long getIteration() {
    return model.getIteration();
  }

  @Override
  public byte[] getData(String key) {
    if(key.equals(MODEL_KEY)) {
      try {
        setData(MODEL_KEY, serializeModel());
      } catch (IOException e) {}
    }
    return super.getData(key);
  }

  public byte[] serialize() throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    serialize(byteOut);
    return byteOut.toByteArray();
  }

  public void serialize(OutputStream out) throws IOException {
    setData(MODEL_KEY, serializeModel());
    dataStore.serialize(out);
  }

  private byte[] serializeModel() throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try (OutputStream out = new GZIPOutputStream(byteOut)) {
      serializeModel(model, out);
    }
    return byteOut.toByteArray();
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
}
