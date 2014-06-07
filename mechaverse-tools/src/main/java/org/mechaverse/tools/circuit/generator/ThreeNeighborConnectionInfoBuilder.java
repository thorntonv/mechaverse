package org.mechaverse.tools.circuit.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mechaverse.circuit.model.Element;
import org.mechaverse.circuit.model.ElementType;
import org.mechaverse.circuit.model.Output;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.ConnectionInfo;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.Input;
import org.mechaverse.tools.circuit.generator.ExternalElement.ExternalElementType;

import com.google.common.collect.ImmutableList;

/**
 * Connects the given matrix of elements so that every element is connected to 3 of its neighbors.
 * Each element will be connected to its left and right neighbors and alternating elements will be
 * connected to the neighbors above and below. External inputs are created for the missing neighbors
 * on the boundary.
 */
public class ThreeNeighborConnectionInfoBuilder implements ConnectionInfoBuilder {

  private final Element[][] elements;
  private final Map<String, ElementType> elementTypeMap;
  private final Map<Element, Input[]> inputMap = new LinkedHashMap<>();
  private final List<ExternalElement> externalElements = new ArrayList<>();
  private final ConnectionInfo connectionInfo;

  public ThreeNeighborConnectionInfoBuilder(Element[][] elements,
      Map<String, ElementType> elementTypeMap) {
    this.elements = elements;
    this.elementTypeMap = elementTypeMap;
    this.connectionInfo = buildConnectionInfo();
  }

  @Override
  public ConnectionInfo build() {
    return connectionInfo;
  }

  protected ConnectionInfo buildConnectionInfo() {
    int rowCount = elements.length;
    for (int row = 0; row < rowCount; row++) {
      int colCount = elements[row].length;
      for (int col = 0; col < colCount; col++) {
        Element element = elements[row][col];
        Input[] inputs = new Input[3];

        // Connect to left element.
        if (col > 0) {
          inputs[0] = createElementInput(elements[row][col - 1], 2);
        } else {
          inputs[0] = createExternalInput(0, -1, elements[row][colCount - 1], 2);
        }
        // Alternately connect to the row above and below.
        if(col % 2 == 0) {
          // Connect to the row above.
          if (row > 0) {
            inputs[1] = createElementInput(elements[row - 1][col], 1);
          } else {
            inputs[1] = createExternalInput(-1, 0, elements[rowCount-1][col], 1);
          }
        } else {
          // Connect to the row below.
          if (row < elements.length - 1) {
            inputs[1] = createElementInput(elements[row + 1][col], 1);
          } else {
            inputs[1] = createExternalInput(1, 0, elements[0][col], 1);
          }
        }

        // Connect to the right element.
        if (col < elements[row].length - 1) {
          inputs[2] = createElementInput(elements[row][col + 1], 0);
        } else {
          inputs[2] = createExternalInput(0, 1, elements[row][0], 0);
        }
        inputMap.put(element, inputs);
      }
    }

    return new ConnectionInfo(inputMap, ImmutableList.copyOf(externalElements));
  }

  protected Input createExternalInput(int relativeUnitRow, int relativeUnitCol,
                                      Element element, int outputIdx) {
    ExternalElement externalElement =
        createExternalElement(relativeUnitRow, relativeUnitCol, element, outputIdx);
    Input input = new Input(externalElement, ExternalElementType.INSTANCE.getOutput());
    return input;
  }

  protected ExternalElement createExternalElement(int relativeUnitRow, int relativeUnitCol,
      Element element, int outputIdx) {
    int id = externalElements.size() + 1;
    ElementType elementType = elementTypeMap.get(element.getType());
    Output output = elementType.getOutputs().get(outputIdx);
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
