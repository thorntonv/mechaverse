package org.mechaverse.tools.circuit;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.tools.circuit.generator.java.CircuitSimulation;
import org.mechaverse.tools.circuit.generator.java.JavaCircuitCompiler;

public class CircuitSimulator {

  public static void main(String[] args) throws Exception {
    JAXBContext jaxbContext = JAXBContext.newInstance(Circuit.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    File XMLfile = new File("circuit.xml");
    Circuit circuit = (Circuit) jaxbUnmarshaller.unmarshal(XMLfile);

    JavaCircuitCompiler circuitCompiler = new JavaCircuitCompiler();

    CircuitSimulation circuitSimulation = circuitCompiler.compile(circuit);
    circuitSimulation.update(0, new int[1000]);
  }
}
