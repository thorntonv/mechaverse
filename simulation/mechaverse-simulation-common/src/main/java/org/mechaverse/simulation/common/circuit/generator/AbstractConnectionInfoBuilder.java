package org.mechaverse.simulation.common.circuit.generator;

import java.util.List;
import java.util.Map;

import org.mechaverse.circuit.model.Element;
import org.mechaverse.circuit.model.ElementType;
import org.mechaverse.circuit.model.Output;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ConnectionInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.Input;
import org.mechaverse.simulation.common.circuit.generator.ExternalElement.ExternalElementType;

/**
 * Connects elements in the matrix.
 */
public abstract class AbstractConnectionInfoBuilder implements ConnectionInfoBuilder {

  protected final Element[][] elements;
  protected final Map<String, ElementType> elementTypeMap;

  public AbstractConnectionInfoBuilder(Element[][] elements,
      Map<String, ElementType> elementTypeMap) {
    this.elements = elements;
    this.elementTypeMap = elementTypeMap;
  }

  @Override
  public abstract ConnectionInfo build();

  protected Input createExternalInput(int relativeUnitRow, int relativeUnitCol,
      Element element, int outputIdx, List<ExternalElement> externalElements) {
    ExternalElement externalElement =
        createExternalElement(relativeUnitRow, relativeUnitCol, element, outputIdx, externalElements);
    Input input = new Input(externalElement, ExternalElementType.INSTANCE.getOutput());
    return input;
  }

  protected ExternalElement createExternalElement(int relativeUnitRow, int relativeUnitCol,
      Element element, int outputIdx, List<ExternalElement> externalElements) {
    int id = externalElements.size() + 1;
    ElementType elementType = elementTypeMap.get(element.getType());
    Output output = elementType.getOutputs().get(outputIdx % elementType.getOutputs().size());
    ExternalElement externalElement =
        new ExternalElement(relativeUnitRow, relativeUnitCol, element.getId(), output.getId());
    externalElement.setId(CircuitSimulationModel.EXTERNAL_INPUT_ID_PREFIX + String.valueOf(id));
    externalElements.add(externalElement);
    return externalElement;
  }

  protected Input createElementInput(Element element, int outputIdx) {
    ElementType elementType = elementTypeMap.get(element.getType());
    return new Input(element, elementType.getOutputs().get(
        outputIdx % elementType.getOutputs().size()));
  }
}