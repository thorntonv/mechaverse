package org.mechaverse.simulation.common.cellautomaton.simulation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;

/**
 * Reads a {@link CellularAutomatonDescriptor} from XML.
 */
public class CellularAutomatonDescriptorReader {

  /**
   * Reads a {@link CellularAutomatonDescriptor} from the given input XML.
   */
  public static CellularAutomatonDescriptor read(byte[] xmlData) throws IOException {
    return read(new ByteArrayInputStream(xmlData));
  }

  /**
   * Reads a {@link CellularAutomatonDescriptor} from the given input XML.
   */
  public static CellularAutomatonDescriptor read(InputStream in) throws IOException {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(CellularAutomatonDescriptor.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (CellularAutomatonDescriptor) jaxbUnmarshaller.unmarshal(in);
    } catch (JAXBException e) {
      throw new IOException(e);
    }
  }
}
