package org.mechaverse.simulation.common.circuit.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mechaverse.circuit.model.Element;
import org.mechaverse.circuit.model.ElementType;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ConnectionInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.Input;

import com.google.common.collect.ImmutableList;

/**
 * Connects the given matrix of elements so that every element is connected to 8 of its neighbors.
 * Each element will be connected to its neighbors to the left, right, above and below as well as
 * the four diagonals. External inputs are created for the missing neighbors on the boundary.
 * 
 * <pre>
 * 1 2 3
 * 0 - 4
 * 7 6 5
 * <pre>
 */
public class EightNeighborConnectionInfoBuilder extends AbstractConnectionInfoBuilder {

  private final ConnectionInfo connectionInfo;

  public EightNeighborConnectionInfoBuilder(Element[][] elements,
      Map<String, ElementType> elementTypeMap) {
    super(elements, elementTypeMap);
    this.connectionInfo = buildConnectionInfo();
  }

  @Override
  public ConnectionInfo build() {
    return connectionInfo;
  }

  protected ConnectionInfo buildConnectionInfo() {
    Map<Element, Input[]> inputMap = new LinkedHashMap<>();
    List<ExternalElement> externalElements = new ArrayList<>();

    int rowCount = elements.length;
    for (int row = 0; row < rowCount; row++) {
      int colCount = elements[row].length;
      for (int col = 0; col < colCount; col++) {
        Element element = elements[row][col];
        Input[] inputs = new Input[8];

        // Connect to left element.
        if (col > 0) {
          inputs[0] = createElementInput(elements[row][col - 1], 4);
        } else {
          inputs[0] = createExternalInput(0, -1, elements[row][colCount - 1], 4, externalElements);
        }
        
        // Connect to the upper left element.
        if (col > 0 && row > 0) {
          inputs[1] = createElementInput(elements[row - 1][col - 1], 5);
        } else {
          inputs[1] =
              createExternalInput(-1, -1, elements[rowCount - 1][colCount - 1], 5, externalElements);
        }
              
        // Connect to the row above.
        if (row > 0) {
          inputs[2] = createElementInput(elements[row - 1][col], 6);
        } else {
          inputs[2] = createExternalInput(-1, 0, elements[rowCount - 1][col], 6, externalElements);
        }
        
        // Connect to the upper right element.
        if (col < elements[row].length - 1 && row > 0) {
          inputs[3] = createElementInput(elements[row - 1][col + 1], 7);
        } else {
          inputs[3] = createExternalInput(-1, 1, elements[rowCount - 1][0], 7, externalElements);
        }
        
        // Connect to the right element.
        if (col < elements[row].length - 1) {
          inputs[4] = createElementInput(elements[row][col + 1], 0);
        } else {
          inputs[4] = createExternalInput(0, 1, elements[row][0], 0, externalElements);
        }
        
        // Connect to the lower right element.
        if (col < elements[row].length - 1 && row < elements.length - 1) {
          inputs[5] = createElementInput(elements[row + 1][col + 1], 1);
        } else {
          inputs[5] = createExternalInput(1, 1, elements[0][0], 1, externalElements);
        }
                
        // Connect to the row below.
        if (row < elements.length - 1) {
          inputs[6] = createElementInput(elements[row + 1][col], 2);
        } else {
          inputs[6] = createExternalInput(1, 0, elements[0][col], 2, externalElements);
        }
        
        // Connect to the lower left element.
        if (col > 0 && row < elements.length - 1) {
          inputs[7] = createElementInput(elements[row + 1][col - 1], 3);
        } else {
          inputs[7] = createExternalInput(1, -1, elements[0][colCount - 1], 3, externalElements);
        }
        
        inputMap.put(element, inputs);
      }
    }

    return new ConnectionInfo(inputMap, ImmutableList.copyOf(externalElements));
  }
}
