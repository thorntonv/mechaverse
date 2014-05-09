package org.mechaverse.tools.circuit.generator;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.circuit.model.Element;
import org.mechaverse.circuit.model.ElementType;
import org.mechaverse.circuit.model.Output;
import org.mechaverse.circuit.model.Param;

import com.google.common.collect.Iterables;

/**
 * A model used for generating a circuit simulation.
 */
public class CircuitSimulationModel {

  public static final String EXTERNAL_INPUT_ID_PREFIX = "in";

  /**
   * An input that is connected to the output of an element.
   */
  public static class Input {

    private Element element;
    private Output output;

    public Input(Element element, Output output) {
      this.element = element;
      this.output = output;
    }

    public Element getElement() {
      return element;
    }

    public Output getOutput() {
      return output;
    }
  }

  /**
   * Information about an {@link Element}.
   */
  public static class ElementInfo {

    private final Element element;
    private final ElementType elementType;
    private final Input[] inputs;
    private final Map<String, String> outputVarNameMap;
    private final Map<String, String> paramVarNameMap;
    // Maps an output id to a param id map.
    private final Map<String, Map<String, String>> outputParamIdMap;

    public ElementInfo(Element element, ElementType elementType, Input[] inputs,
        Map<String, String> outputVarNameMap, Map<String, String> paramVarNameMap,
        Map<String, Map<String, String>> outputParamIdMap) {
      this.element = element;
      this.elementType = elementType;
      this.inputs = inputs;
      this.outputVarNameMap = outputVarNameMap;
      this.paramVarNameMap = paramVarNameMap;
      this.outputParamIdMap = outputParamIdMap;
    }

    public String getId() {
      return element.getId();
    }

    public Element getElement() {
      return element;
    }

    public ElementType getType() {
      return elementType;
    }

    public List<Output> getOutputs() {
      return elementType.getOutputs();
    }

    public Input[] getInputs() {
      return inputs;
    }

    public String getOutputVarName(Output output) {
      return getOutputVarName(output.getId());
    }

    public String getOutputVarName(String outputId) {
      return outputVarNameMap.get(outputId);
    }

    public Collection<String> getOutputVarNames() {
      return outputVarNameMap.values();
    }

    public String getParamVarName(Param param) {
      return getParamVarName(param.getId());
    }

    public String getParamVarName(String paramId) {
      return paramVarNameMap.get(paramId);
    }

    public Collection<String> getParamVarNames() {
      return paramVarNameMap.values();
    }

    public String getOutputParamVarName(Output output, Param param) {
      return getOutputParamVarName(output, param.getId());
    }

    public String getOutputParamVarName(Output output, String paramId) {
      Map<String, String> paramIdMap = outputParamIdMap.get(output.getId());
      if (paramIdMap != null) {
        return getParamVarName(paramIdMap.get(paramId));
      }
      return null;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      ElementInfo other = (ElementInfo) obj;
      if (element == null) {
        if (other.element != null) return false;
      } else if (!element.getId().equals(other.element.getId())) return false;
      return true;
    }

    @Override
    public int hashCode() {
      return element.getId().hashCode();
    }
  }

  /**
   * Information about a {@link LogicalUnit).
   */
  public static class LogicalUnitInfo {

    private final List<ElementInfo> elements;
    private final List<ElementInfo> externalElements;

    public LogicalUnitInfo(List<ElementInfo> elements, List<ElementInfo> externalElements) {
      this.elements = elements;
      this.externalElements = externalElements;
    }

    public List<ElementInfo> getElements() {
      return elements;
    }

    public List<ElementInfo> getExternalElements() {
      return externalElements;
    }

    /**
     * @return the element with the given id
     */
    public ElementInfo getElementInfo(String id) {
      for (ElementInfo element : Iterables.concat(getElements(), getExternalElements())) {
        if (element.getId().equals(id)) {
          return element;
        }
      }
      return null;
    }
  }

  protected final Circuit circuit;
  protected final Map<String, ElementType> elementTypeMap;
  protected final LogicalUnitInfo logicalUnitInfo;

  public CircuitSimulationModel(Circuit circuit, Map<String, ElementType> elementTypeMap,
      LogicalUnitInfo logicalUnitInfo) {
    this.circuit = circuit;
    this.logicalUnitInfo = logicalUnitInfo;
    this.elementTypeMap = elementTypeMap;
  }

  public Circuit getCircuit() {
    return circuit;
  }

  /**
   * @return the type of the element with the given id
   */
  public ElementType getElementType(String typeId) {
    return elementTypeMap.get(typeId);
  }

  public LogicalUnitInfo getLogicalUnitInfo() {
    return logicalUnitInfo;
  }
}
