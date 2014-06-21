package org.mechaverse.simulation.common.circuit;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationGenerator;
import org.mechaverse.simulation.common.circuit.generator.java.JavaCircuitGeneratorImpl;
import org.mechaverse.simulation.common.circuit.generator.opencl.OpenClCircuitGeneratorImpl;

/**
 * Generates source code for simulating a circuit.
 *
 * @author thorntonv@mechaverse.org
 */
public class CircuitGenerator {

  private static final String DEFAULT_TYPE = "opencl";

  public static void main(String[] args) throws Exception {
    Options options = buildOptions();

    try {
      CommandLineParser parser = new GnuParser();
      CommandLine cmd = parser.parse(options, args);

      String type = cmd.getOptionValue('t', DEFAULT_TYPE);
      String inputFilename = cmd.getOptionValue('i');
      String outputFilename = cmd.getOptionValue('o');

      JAXBContext jaxbContext = JAXBContext.newInstance(Circuit.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      File XMLfile = new File(inputFilename);
      Circuit circuit = (Circuit) jaxbUnmarshaller.unmarshal(XMLfile);
      CircuitSimulationGenerator generator = createGenerator(type, circuit);
      PrintWriter out = new PrintWriter(System.out);
      if (outputFilename != null) {
        out = new PrintWriter(new FileWriter(new File(outputFilename)));
      }
      generator.generate(out);
    } catch (ParseException ex) {
      System.out.println(ex.getMessage());
      System.out.println();
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(CircuitGenerator.class.getName(), options);
    }
  }

  private static final Options buildOptions() {
    Options options = new Options();

    OptionGroup group = new OptionGroup();
    group.addOption(new Option("i", "input", true, "Circuit XML file name"));
    group.setRequired(true);
    options.addOption(new Option("o", "output", true, "Output file name"));
    options.addOption(new Option("t", "type", true, "Type of source to generate (opencl, java)"));

    options.addOptionGroup(group);
    return options;
  }

  private static CircuitSimulationGenerator createGenerator(String type, Circuit circuit) {
    if (type.equalsIgnoreCase(JavaCircuitGeneratorImpl.TYPE)) {
      return new JavaCircuitGeneratorImpl(circuit);
    } else if (type.equalsIgnoreCase(OpenClCircuitGeneratorImpl.TYPE)) {
      return new OpenClCircuitGeneratorImpl(circuit);
    }
    throw new IllegalArgumentException(type + " is not a valid circuit generator type.");
  }
}
