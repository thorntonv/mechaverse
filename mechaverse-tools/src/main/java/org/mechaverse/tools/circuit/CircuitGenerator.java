package org.mechaverse.tools.circuit;

import java.io.File;
import java.io.PrintWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.tools.circuit.generator.CircuitSimulationGenerator;
import org.mechaverse.tools.circuit.generator.java.JavaCircuitGeneratorImpl;

public class CircuitGenerator {

  public static void main(String[] args) throws Exception {
    if(args.length == 0) {
      System.out.println("Usage: " + CircuitGenerator.class.getName() + " [circuit-xml-file]");
      return;
    }

    JAXBContext jaxbContext = JAXBContext.newInstance(Circuit.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    File XMLfile = new File(args[0]);
    Circuit circuit = (Circuit) jaxbUnmarshaller.unmarshal(XMLfile);
    CircuitSimulationGenerator generator = new JavaCircuitGeneratorImpl(circuit);
    generator.generate(new PrintWriter(System.out));
  }
}
