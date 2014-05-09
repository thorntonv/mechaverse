package org.mechaverse.tools.circuit.generator;

import java.util.LinkedHashMap;
import java.util.Map;

import org.mechaverse.circuit.model.Element;
import org.mechaverse.circuit.model.ElementType;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.Input;
import org.mechaverse.tools.circuit.generator.ExternalElement.ExternalElementType;

/**
 * Connects the given matrix of elements so that every element is connected to 3 of its neighbors.
 * Each element will be connected to its left and right neighbors and alternating elements will be
 * connected to the neighbors above and below. External inputs are created for the missing neighbors
 * on the boundary.
 */
public class ThreeNeighborElementInputMapBuilder implements ElementInputMapBuilder {

  private final Element[][] elements;
  private final Map<String, ElementType> elementTypeMap;

  public ThreeNeighborElementInputMapBuilder(Element[][] elements,
      Map<String, ElementType> elementTypeMap) {
    this.elements = elements;
    this.elementTypeMap = elementTypeMap;
  }

  @Override
  public Map<Element, Input[]> build() {
    Map<Element, Input[]> inputMap = new LinkedHashMap<>();

    int externalElementId = 1;
    for (int row = 0; row < elements.length; row++) {
      for (int col = 0; col < elements[row].length; col++) {
        Element element = elements[row][col];
        Input[] inputs = new Input[3];

        // Connect to left element.
        if (col > 0) {
          inputs[0] = createElementInput(elements[row][col - 1], 2);
        } else {
          inputs[0] = createExternalInput(externalElementId++);
          inputMap.put(inputs[0].getElement(), new Input[]{});
        }
        // Alternately connect to the row above and below.
        if (col % 2 == 0 && row > 0) {
          // Connect to the row above.
          inputs[1] = createElementInput(elements[row - 1][col], 1);
        } else if (col % 2 == 1 && row < elements.length - 1) {
          // Connect to the row below.
          inputs[1] = createElementInput(elements[row + 1][col], 1);
        } else {
          inputs[1] = createExternalInput(externalElementId++);
          inputMap.put(inputs[1].getElement(), new Input[]{});
        }

        // Connect to the right element.
        if (col < elements[row].length - 1) {
          inputs[2] = createElementInput(elements[row][col + 1], 0);
        } else {
          inputs[2] = createExternalInput(externalElementId++);
          inputMap.put(inputs[2].getElement(), new Input[]{});
        }
        inputMap.put(element, inputs);
      }
    }

    return inputMap;
  }

  protected Input createExternalInput(int id) {
    ExternalElement externalElement = new ExternalElement();
    externalElement.setId(CircuitSimulationModel.EXTERNAL_INPUT_ID_PREFIX + String.valueOf(id));
    Input input = new Input(externalElement, ExternalElementType.INSTANCE.getOutput());
    return input;
  }

  protected Input createElementInput(Element element, int outputIdx) {
    ElementType elementType = elementTypeMap.get(element.getType());
    return new Input(element, elementType.getOutputs().get(outputIdx));
  }
}
