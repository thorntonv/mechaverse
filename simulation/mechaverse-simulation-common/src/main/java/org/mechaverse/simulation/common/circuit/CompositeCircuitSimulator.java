package org.mechaverse.simulation.common.circuit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * A circuit simulator that combines multiple circuit simulators.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public final class CompositeCircuitSimulator implements CircuitSimulator {

  /**
   * The exception that is thrown when an error occurs closing the circuit simulator.
   */
  public static class CompositeCircuitSimulatorCloseException extends Exception {

    private static final long serialVersionUID = 1L;

    private final List<Exception> exceptions;

    public CompositeCircuitSimulatorCloseException(String message, List<Exception> exceptions) {
      super(message, exceptions.get(0));
      this.exceptions = exceptions;
    }

    public List<Exception> getExceptions() {
      return exceptions;
    }
  }

  private static class MappedCircuitInfo {
    public final int index;
    public final CircuitSimulator simulator;

    public MappedCircuitInfo(int index, CircuitSimulator simulator) {
      this.index = index;
      this.simulator = simulator;
    }
  }

  private CircuitSimulator[] simulators;
  private final CircuitAllocator allocator;
  private MappedCircuitInfo[] mappedCircuitInfo;

  public CompositeCircuitSimulator(List<CircuitSimulator> simulators) {
    Preconditions.checkNotNull(simulators);
    Preconditions.checkState(simulators.size() > 0);

    this.simulators = simulators.toArray(new CircuitSimulator[simulators.size()]);
    this.mappedCircuitInfo = buildMappedCircuitInfo(simulators);
    this.allocator = new CircuitAllocator(mappedCircuitInfo.length);
  }

  @Override
  public CircuitAllocator getAllocator() {
    return allocator;
  }

  @Override
  public int getCircuitCount() {
    return mappedCircuitInfo.length;
  }

  @Override
  public int getCircuitInputSize() {
    return simulators[0].getCircuitInputSize();
  }

  @Override
  public int getCircuitStateSize() {
    return simulators[0].getCircuitStateSize();
  }

  @Override
  public int getCircuitOutputSize() {
    return simulators[0].getCircuitOutputSize();
  }

  @Override
  public void getCircuitState(int circuitIndex, int[] circuitState) {
    MappedCircuitInfo circuit = mappedCircuitInfo[circuitIndex];
    circuit.simulator.getCircuitState(circuit.index, circuitState);
  }

  @Override
  public void setCircuitState(int circuitIndex, int[] circuitState) {
    MappedCircuitInfo circuit = mappedCircuitInfo[circuitIndex];
    circuit.simulator.setCircuitState(circuit.index, circuitState);
  }

  @Override
  public void setCircuitInput(int circuitIndex, int[] circuitInput) {
    MappedCircuitInfo circuit = mappedCircuitInfo[circuitIndex];
    circuit.simulator.setCircuitInput(circuit.index, circuitInput);
  }

  @Override
  public void setCircuitOutputMap(int circuitIndex, int[] outputMap) {
    MappedCircuitInfo circuit = mappedCircuitInfo[circuitIndex];
    circuit.simulator.setCircuitOutputMap(circuit.index, outputMap);
  }

  @Override
  public void getCircuitOutput(int circuitIndex, int[] circuitOutput) {
    MappedCircuitInfo circuit = mappedCircuitInfo[circuitIndex];
    circuit.simulator.getCircuitOutput(circuit.index, circuitOutput);
  }

  @Override
  public void update() {
    for (CircuitSimulator simulator : simulators) {
      simulator.update();
    }
  }

  @Override
  public void close() throws CompositeCircuitSimulatorCloseException {
    List<Exception> exceptions = new ArrayList<>();
    for (CircuitSimulator simulator : simulators) {
      try {
        simulator.close();
      } catch (Exception ex) {
        exceptions.add(ex);
      }
    }
    if (exceptions.size() > 0) {
      throw new CompositeCircuitSimulatorCloseException(
          "An error occured while closing a composite circuit simulator: "
              + exceptions.get(0).getMessage(), exceptions);
    }
  }

  private static MappedCircuitInfo[] buildMappedCircuitInfo(List<CircuitSimulator> simulators) {
    List<MappedCircuitInfo> mappedCircuitInfo = new ArrayList<>();

    List<CircuitSimulator> simulatorsToProcess = new ArrayList<>(simulators);
    Iterator<CircuitSimulator> simulatorIt = simulatorsToProcess.iterator();
    while (simulatorsToProcess.size() > 0) {
      if (simulatorIt.hasNext()) {
        CircuitSimulator simulator = simulatorIt.next();
        if (simulator.getAllocator().getAvailableCircuitCount() > 0) {
          int mappedCircuitIdx = simulator.getAllocator().allocateCircuit();
          mappedCircuitInfo.add(new MappedCircuitInfo(mappedCircuitIdx, simulator));
        } else {
          simulatorIt.remove();
        }
      } else {
        simulatorIt = simulatorsToProcess.iterator();
      }
    }

    return mappedCircuitInfo.toArray(new MappedCircuitInfo[mappedCircuitInfo.size()]);
  }
}
