package org.mechaverse.simulation.common.circuit;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.mechaverse.circuit.model.Circuit;

/**
 * Reads a circuit from a given input XML stream.
 */
public class CircuitReader {

  /**
   * Reads a circuit from the given input XML.
   */
  public static Circuit read(InputStream in) throws IOException {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Circuit.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (Circuit) jaxbUnmarshaller.unmarshal(in);
    } catch (JAXBException e) {
      throw new IOException(e);
    }
  }
}
