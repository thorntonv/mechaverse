package org.mechaverse.simulation.common.cellautomaton.simulation.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonDescriptorReader;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.java.JavaCellularAutomatonGeneratorImpl;
import org.mechaverse.simulation.common.cellautomaton.simulation.opencl.OpenClCellularAutomatonGeneratorImpl;

/**
 * Command line interface for generating cellular automaton simulation source code.
 */
public class CellularAutomatonGeneratorCLI {

  private static final String DEFAULT_TYPE = "opencl";

  public static void main(String[] args) throws Exception {
    Options options = buildOptions();

    try {
      CommandLineParser parser = new GnuParser();
      CommandLine cmd = parser.parse(options, args);

      String type = cmd.getOptionValue('t', DEFAULT_TYPE);
      String inputFilename = cmd.getOptionValue('i');
      String outputFilename = cmd.getOptionValue('o');

      CellularAutomatonDescriptor descriptor = CellularAutomatonDescriptorReader.read(
          new FileInputStream(inputFilename));
      CellularAutomatonSimulationGenerator generator = createGenerator(type, descriptor);
      PrintWriter out = new PrintWriter(System.out);
      if (outputFilename != null) {
        out = new PrintWriter(new FileWriter(new File(outputFilename)));
      }

      generator.generate(out);
      out.close();
    } catch (ParseException ex) {
      System.out.println(ex.getMessage());
      System.out.println();
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(CellularAutomatonGeneratorCLI.class.getName(), options);
    }
  }

  private static Options buildOptions() {
    Options options = new Options();

    OptionGroup group = new OptionGroup();
    group.addOption(new Option("i", "input", true, "Cellular automaton descriptor XML file name"));
    group.setRequired(true);
    options.addOption(new Option("o", "output", true, "Output file name"));
    options.addOption(new Option("t", "type", true, "Type of source to generate (opencl, java)"));

    options.addOptionGroup(group);
    return options;
  }

  private static CellularAutomatonSimulationGenerator createGenerator(
      String type, CellularAutomatonDescriptor descriptor) {
    if (type.equalsIgnoreCase(OpenClCellularAutomatonGeneratorImpl.TYPE)) {
      return new OpenClCellularAutomatonGeneratorImpl(descriptor);
    } else if (type.equalsIgnoreCase(JavaCellularAutomatonGeneratorImpl.TYPE)) {
      return new JavaCellularAutomatonGeneratorImpl(descriptor, 1, 1);
    }
    throw new IllegalArgumentException(type + " is not a valid generator type.");
  }
}
