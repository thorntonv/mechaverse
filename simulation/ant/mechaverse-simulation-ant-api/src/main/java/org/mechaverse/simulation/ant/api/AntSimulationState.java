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
    SimulationDataStore dataStore = SimulationDataStore.deserialize(data);
    return new AntSimulationState(dataStore);
  }

  public AntSimulationState() {
    super(new SimulationModel());
  }

  public AntSimulationState(SimulationDataStore dataStore) throws IOException {
    super(deserializeModel(dataStore.get(MODEL_KEY)));
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
    setData(MODEL_KEY, serializeModel());
    return dataStore.serialize();
  }

  private byte[] serializeModel() throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try (OutputStream out = new GZIPOutputStream(byteOut)) {
      JAXBContext jaxbContext = JAXBContext.newInstance(SimulationModel.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.marshal(model, out);
    } catch (JAXBException e) {
      throw new IOException(e);
    }
    return byteOut.toByteArray();
  }

  public static SimulationModel deserializeModel(byte[] data) throws IOException {
    if (data == null) {
      return new SimulationModel();
    }
    try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(data))) {
      JAXBContext jaxbContext = JAXBContext.newInstance(SimulationModel.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (SimulationModel) jaxbUnmarshaller.unmarshal(in);
    } catch (JAXBException e) {
      throw new IOException(e);
    }
  }
}
