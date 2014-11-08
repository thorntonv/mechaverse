package org.mechaverse.simulation.common.circuit.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.circuit.model.Element;
import org.mechaverse.circuit.model.ElementType;
import org.mechaverse.circuit.model.LogicalUnit;
import org.mechaverse.circuit.model.Output;
import org.mechaverse.circuit.model.Param;
import org.mechaverse.circuit.model.Row;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ConnectionInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ElementInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ExternalElementInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.Input;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.LogicalUnitInfo;
import org.mechaverse.simulation.common.circuit.generator.ExternalElement.ExternalElementType;
import org.springframework.beans.BeanUtils;

import com.google.common.collect.ImmutableBiMap;

/**
 * Builds a {@link CircuitSimulationModel} for a {@link Circuit}.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class CircuitSimulationModelBuilder {

  private static final Pattern ID_PATTERN = Pattern.compile("\\d+");

  protected abstract class AbstractElementInfoBuilder<E extends Element, I extends ElementInfo> {

    protected final E element;
    protected final ElementType elementType;
    protected final Input[] inputs;

    public AbstractElementInfoBuilder(E element, ElementType elementType, Input[] inputs) {
      this.element = element;
      this.elementType = elementType;
      this.inputs = inputs;
    }

    public I build() {
      Map<String, String> outputVarNameMap = new HashMap<>();
      Map<String, String> paramVarNameMap = new HashMap<>();
      Map<String, Map<String, String>> outputParamIdMap = new HashMap<>();

      // Build a map that maps a param id (used to refer to the parameter in output expressions) to
      // its variable name.
      for (Param param : elementType.getParams()) {
        String varName = getElementParamVarName(element, param);
        if (!paramVarNameMap.containsKey(param.getId())) {
          paramVarNameMap.put(param.getId(), varName);
        } else {
          throw new IllegalStateException("Parameter " + param.getId()
              + " is defined more than once for element " + element.getId());
        }
      }
      for (Output output : elementType.getOutputs()) {
        outputVarNameMap.put(output.getId(), getElementOutputVarName(element, output));
        Map<String, String> paramIdMap = new HashMap<>();
        for (Param param : output.getParams()) {
          String id = getElementOutputParamId(output, param.getId());
          String varName = getElementOutputParamVarName(element, output, param);
          if (!paramVarNameMap.containsKey(id)) {
            paramIdMap.put(param.getId(), id);
            paramVarNameMap.put(id, varName);
          } else {
            throw new IllegalStateException("Parameter " + id
                + " is defined more than once for element " + element.getId());
          }
        }
        outputParamIdMap.put(output.getId(), paramIdMap);
      }
      return createElementInfo(outputVarNameMap, paramVarNameMap, outputParamIdMap);
    }

    protected abstract I createElementInfo(Map<String, String> outputVarNameMap,
        Map<String, String> paramVarNameMap, Map<String, Map<String, String>> outputParamIdMap);
  }

  protected class ElementInfoBuilder extends AbstractElementInfoBuilder<Element, ElementInfo> {

    public ElementInfoBuilder(Element element, ElementType elementType, Input[] inputs) {
      super(element, elementType, inputs);
    }

    @Override
    protected ElementInfo createElementInfo(Map<String, String> outputVarNameMap,
        Map<String, String> paramVarNameMap, Map<String, Map<String, String>> outputParamIdMap) {
      return new ElementInfo(element, elementType, inputs, outputVarNameMap, paramVarNameMap,
          outputParamIdMap);
    }
  }

  protected class ExternalElementInfoBuilder
      extends AbstractElementInfoBuilder<ExternalElement, ExternalElementInfo> {

    public ExternalElementInfoBuilder(ExternalElement element) {
      super(element, ExternalElementType.INSTANCE, new Input[] {});
    }

    @Override
    protected ExternalElementInfo createElementInfo(Map<String, String> outputVarNameMap,
        Map<String, String> paramVarNameMap, Map<String, Map<String, String>> outputParamIdMap) {
      return new ExternalElementInfo(element, outputVarNameMap);
    }
  }

  /**
   * Builds the simulation model for the given circuit.
   */
  public static CircuitSimulationModel build(Circuit circuit) {
    return new CircuitSimulationModelBuilder().buildModel(circuit);
  }

  public CircuitSimulationModel buildModel(Circuit circuit) {
    Map<String, ElementType> elementTypeMap = buildElementTypeMap(circuit);
    LogicalUnitInfo logicalUnitInfo =
        buildLogicalUnitInfo(circuit.getLogicalUnit(), elementTypeMap);
    return new CircuitSimulationModel(circuit, elementTypeMap, logicalUnitInfo);
  }

  private Map<String, ElementType> buildElementTypeMap(Circuit circuit) {
    Map<String, ElementType> map = new HashMap<>();
    map.put(ExternalElement.TYPE, ExternalElementType.INSTANCE);
    for (ElementType elementType : circuit.getElementTypes()) {
      map.put(elementType.getId(), preprocessElementType(elementType));
    }
    return map;
  }

  /**
   * Preprocesses an element type. If an output has an id that specified multiple outputs eg.
   * {1,2,3} then the output will be duplicated for each of the specified ids.
   */
  private ElementType preprocessElementType(ElementType elementType) {
    ElementType processedElementType = new ElementType();
    processedElementType.setId(elementType.getId());
    processedElementType.getParams().addAll(elementType.getParams());

    for (Output output : elementType.getOutputs()) {
      Matcher matcher = ID_PATTERN.matcher(output.getId());
      while (matcher.find()) {
        Output newOutput = new Output();
        newOutput.setId(matcher.group());
        newOutput.getParams().addAll(output.getParams());
        newOutput.setExpression(output.getExpression());
        processedElementType.getOutputs().add(newOutput);
      }
    }
    return processedElementType;
  }

  private LogicalUnitInfo buildLogicalUnitInfo(LogicalUnit unit,
      Map<String, ElementType> elementTypeMap) {
    // Build element matrix.
    Element[][] matrix = new Element[unit.getRows().size()][];
    int id = 1;
    for (int rowIdx = 0; rowIdx < matrix.length; rowIdx++) {
      Row row = unit.getRows().get(rowIdx);
      matrix[rowIdx] = new Element[row.getElements().size()];
      for (int colIdx = 0; colIdx < matrix[rowIdx].length; colIdx++) {
        // Create a copy of the element because it will be modified as part of building the model.
        Element element = new Element();
        BeanUtils.copyProperties(row.getElements().get(colIdx), element);

        if (element.getId() == null) {
          // If the element does not have an id assign one.
          element.setId(String.valueOf(id));
          id++;
        }
        matrix[rowIdx][colIdx] = preprocessElement(element, elementTypeMap);
      }
    }

    // Connect elements.
    // TODO(thorntonv): Make connection configurable.
    ConnectionInfoBuilder connectionInfoBuilder =
        new ThreeNeighborConnectionInfoBuilder(matrix, elementTypeMap);
    ConnectionInfo connectionInfo = connectionInfoBuilder.build();

    List<ElementInfo> elements = new ArrayList<>();
    List<ExternalElementInfo> externalElements = new ArrayList<>();
    for (Element element : connectionInfo.getElements()) {
      ElementType elementType = elementTypeMap.get(element.getType());
      Input[] inputs = connectionInfo.getInputs(element);
      elements.add(new ElementInfoBuilder(element, elementType, inputs).build());
    }
    for (ExternalElement externalElement : connectionInfo.getExternalElements()) {
      externalElements.add(new ExternalElementInfoBuilder(externalElement).build());
    }
    return new LogicalUnitInfo(elements, externalElements,
        buildVarNameStateIndexMap(elements, externalElements));
  }

  /**
   * Preprocesses an element. If the outputs attribute is set a new element type will be created for
   * the element that only includes the specified outputs.
   */
  private Element preprocessElement(Element element, Map<String, ElementType> elementTypeMap) {
    if (element.getOutputs() != null) {
      ElementType elementType = elementTypeMap.get(element.getType());
      ElementType newElementType = new ElementType();
      newElementType.getParams().addAll(elementType.getParams());
      newElementType.setId(element.getType() + "_e" + element.getId());

      Matcher matcher = ID_PATTERN.matcher(element.getOutputs());
      while (matcher.find()) {
        String outputId = matcher.group();
        Output output = getOutput(outputId, elementType);
        if (output != null) {
          newElementType.getOutputs().add(output);
        }
      }
      elementTypeMap.put(newElementType.getId(), newElementType);
      element.setType(newElementType.getId());
    }
    return element;
  }

  private Output getOutput(String id, ElementType elementType) {
    for (Output output : elementType.getOutputs()) {
      if (output.getId().equalsIgnoreCase(id)) {
        return output;
      }
    }
    return null;
  }

  protected ImmutableBiMap<String, Integer> buildVarNameStateIndexMap(
      Iterable<ElementInfo> elements, Iterable<ExternalElementInfo> externalElements) {
    int stateIndex = 0;
    ImmutableBiMap.Builder<String, Integer> varNameStateIndexMapBuilder = ImmutableBiMap.builder();

    // Outputs that are the input of an external element come first.
    for (ElementInfo element : elements) {
      for (Output output : element.getOutputs()) {
        if (isExternalInput(element, output, externalElements)) {
          String varName = element.getOutputVarName(output);
          varNameStateIndexMapBuilder.put(varName, stateIndex++);
        }
      }
    }

    for (ElementInfo element : elements) {
      for (Output output : element.getOutputs()) {
        if (!isExternalInput(element, output, externalElements)) {
          String varName = element.getOutputVarName(output);
          varNameStateIndexMapBuilder.put(varName, stateIndex++);
        }
      }
    }

    for (ElementInfo element : elements) {
      for (Param param : element.getType().getParams()) {
        String varName = element.getParamVarName(param);
        varNameStateIndexMapBuilder.put(varName, stateIndex++);
      }
      for (Output output : element.getOutputs()) {
        for (Param param : output.getParams()) {
          String varName = element.getOutputParamVarName(output, param);
          varNameStateIndexMapBuilder.put(varName, stateIndex++);
        }
      }
    }
    return varNameStateIndexMapBuilder.build();
  }

  private boolean isExternalInput(
      ElementInfo element, Output output, Iterable<ExternalElementInfo> externalElements) {
    for (ExternalElementInfo externalElement : externalElements) {
      if (externalElement.getElement().getElementId().equals(element.getId())
          && externalElement.getElement().getOutputId().equals(output.getId())) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return the name of the variable which stores the current value of the given input
   */
  protected String getVarName(Input input) {
    return getElementOutputVarName(input.getElement(), input.getOutput());
  }

  /**
   * @return the name of the variable that stores the current output value of an element
   */
  protected String getElementOutputVarName(Element element, Output output) {
    if(element.getType().equals(ExternalElement.TYPE)) {
      return String.format("ex_%s", element.getId());
    }
    return String.format("e%s_out%s", element.getId(), output.getId());
  }

  /**
   * @return the name of the variable that stores the value of an element parameter
   */
  protected String getElementParamVarName(Element element, Param param) {
    return String.format("e%s_%s", element.getId(), param.getId());
  }

  /**
   * @return the id of an element output parameter that is used to refer to the parameter in
   *         output expressions
   */
  protected String getElementOutputParamId(Output output, String paramId) {
    return String.format("out%s_%s", output.getId(), paramId);
  }

  /**
   * @return the name of the variable that stores the value of an element output parameter
   */
  protected String getElementOutputParamVarName(Element element, Output output, Param param) {
    return String.format("e%s_out%s_%s", element.getId(), output.getId(), param.getId());
  }
}
