package org.mechaverse.tools.circuit.generator;

import java.util.Map;

import org.mechaverse.circuit.model.Element;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.Input;

/**
 * Builder for a map that maps elements to their inputs.
 *
 * @author thorntonv@mechaverse.org
 */
public interface ElementInputMapBuilder {

  public Map<Element, Input[]> build();
}
