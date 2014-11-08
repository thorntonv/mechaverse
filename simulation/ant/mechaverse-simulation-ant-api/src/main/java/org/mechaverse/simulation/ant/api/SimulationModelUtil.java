package org.mechaverse.simulation.ant.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.SimulationModel;

import com.google.common.collect.Lists;

/**
 * Utility methods for {@link SimulationModel}.
 */
public final class SimulationModelUtil {

  private static final int DEFAULT_GZIP_BUFFER_SIZE = 128 * 1024;

  /**
   * @return the environment with the given id from the given state
   */
  public static Environment getEnvironment(SimulationModel simulationModel, String environmentId) {
    if (simulationModel.getEnvironment().getId().equals(environmentId)) {
      return simulationModel.getEnvironment();
    }
    for (Environment subEnvironment : simulationModel.getSubEnvironments()) {
      if (subEnvironment.getId().equals(environmentId)) {
        return subEnvironment;
      }
    }
    return null;
  }

  /**
   * @return a list of the environments in the given state
   */
  public static Collection<Environment> getEnvironments(SimulationModel simulationModel) {
    List<Environment> environments = Lists.newArrayList();
    environments.add(simulationModel.getEnvironment());
    environments.addAll(simulationModel.getSubEnvironments());
    return environments;
  }

  /**
   * Serializes the given model to xml written to the given {@link OutputStream}.
   *
   * @param model the model to serialize
   * @param out the output stream to which serialized data will be written
   */
  public static void serialize(SimulationModel model, OutputStream out) throws IOException {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(SimulationModel.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.marshal(model, out);
    } catch (JAXBException e) {
      throw new IOException(e);
    }
  }

  /**
   * Deserializes xml read from the given input stream to an {@link SimulationModel}.
   *
   * @param in the input stream from which xml will be read
   */
  public static SimulationModel deserialize(InputStream in) throws IOException {
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

  /**
   * Serializes a {@link SimulationModel} to a byte array that contains the compressed xml
   * representation of the model.
   */
  public static byte[] serialize(SimulationModel model) throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try (OutputStream out = new GZIPOutputStream(byteOut, DEFAULT_GZIP_BUFFER_SIZE)) {
      SimulationModelUtil.serialize(model, out);
    }
    return byteOut.toByteArray();
  }

  private SimulationModelUtil() {}
}
