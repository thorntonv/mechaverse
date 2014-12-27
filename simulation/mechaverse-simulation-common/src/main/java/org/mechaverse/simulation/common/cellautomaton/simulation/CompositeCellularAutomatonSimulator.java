package org.mechaverse.simulation.common.cellautomaton.simulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * A cellular automaton simulator that combines multiple simulators.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public final class CompositeCellularAutomatonSimulator implements CellularAutomatonSimulator {

  /**
   * The exception that is thrown when an error occurs closing the simulator.
   */
  public static class CompositeCellularAutomatonSimulatorCloseException extends Exception {

    private static final long serialVersionUID = 1L;

    private final List<Exception> exceptions;

    public CompositeCellularAutomatonSimulatorCloseException(
        String message, List<Exception> exceptions) {
      super(message, exceptions.get(0));
      this.exceptions = exceptions;
    }

    public List<Exception> getExceptions() {
      return exceptions;
    }
  }

  private static class MappedCellularAutomatonInfo {
    public final int index;
    public final CellularAutomatonSimulator simulator;

    public MappedCellularAutomatonInfo(int index, CellularAutomatonSimulator simulator) {
      this.index = index;
      this.simulator = simulator;
    }
  }

  private CellularAutomatonSimulator[] simulators;
  private final CellularAutomatonAllocator allocator;
  private MappedCellularAutomatonInfo[] mappedCellularAutomatonInfo;

  public CompositeCellularAutomatonSimulator(List<CellularAutomatonSimulator> simulators) {
    Preconditions.checkNotNull(simulators);
    Preconditions.checkState(simulators.size() > 0);

    this.simulators = simulators.toArray(new CellularAutomatonSimulator[simulators.size()]);
    this.mappedCellularAutomatonInfo = buildMappedCellularAutomatonInfo(simulators);
    this.allocator = new CellularAutomatonAllocator(mappedCellularAutomatonInfo.length);
  }

  @Override
  public CellularAutomatonAllocator getAllocator() {
    return allocator;
  }

  @Override
  public int size() {
    return mappedCellularAutomatonInfo.length;
  }

  @Override
  public int getAutomatonInputSize() {
    return simulators[0].getAutomatonInputSize();
  }

  @Override
  public int getAutomatonStateSize() {
    return simulators[0].getAutomatonStateSize();
  }

  @Override
  public int getAutomatonOutputSize() {
    return simulators[0].getAutomatonOutputSize();
  }

  @Override
  public void getAutomatonState(int index, int[] state) {
    MappedCellularAutomatonInfo automaton = mappedCellularAutomatonInfo[index];
    automaton.simulator.getAutomatonState(automaton.index, state);
  }

  @Override
  public void setAutomatonState(int index, int[] state) {
    MappedCellularAutomatonInfo automaton = mappedCellularAutomatonInfo[index];
    automaton.simulator.setAutomatonState(automaton.index, state);
  }

  @Override
  public void setAutomatonInput(int index, int[] input) {
    MappedCellularAutomatonInfo automaton = mappedCellularAutomatonInfo[index];
    automaton.simulator.setAutomatonInput(automaton.index, input);
  }

  @Override
  public void setAutomatonOutputMap(int index, int[] outputMap) {
    MappedCellularAutomatonInfo automaton = mappedCellularAutomatonInfo[index];
    automaton.simulator.setAutomatonOutputMap(automaton.index, outputMap);
  }

  @Override
  public void getAutomatonOutput(int index, int[] output) {
    MappedCellularAutomatonInfo automaton = mappedCellularAutomatonInfo[index];
    automaton.simulator.getAutomatonOutput(automaton.index, output);
  }

  @Override
  public void update() {
    for (CellularAutomatonSimulator simulator : simulators) {
      simulator.update();
    }
  }

  @Override
  public void close() throws CompositeCellularAutomatonSimulatorCloseException {
    List<Exception> exceptions = new ArrayList<>();
    for (CellularAutomatonSimulator simulator : simulators) {
      try {
        simulator.close();
      } catch (Exception ex) {
        exceptions.add(ex);
      }
    }
    if (exceptions.size() > 0) {
      throw new CompositeCellularAutomatonSimulatorCloseException(
          "An error occured while closing a composite cellular automaton simulator: "
              + exceptions.get(0).getMessage(), exceptions);
    }
  }

  private static MappedCellularAutomatonInfo[] buildMappedCellularAutomatonInfo(
        List<CellularAutomatonSimulator> simulators) {
    List<MappedCellularAutomatonInfo> mappedCellularAutomatonInfo = new ArrayList<>();

    List<CellularAutomatonSimulator> simulatorsToProcess = new ArrayList<>(simulators);
    Iterator<CellularAutomatonSimulator> simulatorIt = simulatorsToProcess.iterator();
    while (simulatorsToProcess.size() > 0) {
      if (simulatorIt.hasNext()) {
        CellularAutomatonSimulator simulator = simulatorIt.next();
        if (simulator.getAllocator().getAvailableCount() > 0) {
          int mappedAutomatonIdx = simulator.getAllocator().allocate();
          mappedCellularAutomatonInfo.add(
              new MappedCellularAutomatonInfo(mappedAutomatonIdx, simulator));
        } else {
          simulatorIt.remove();
        }
      } else {
        simulatorIt = simulatorsToProcess.iterator();
      }
    }

    return mappedCellularAutomatonInfo.toArray(
        new MappedCellularAutomatonInfo[mappedCellularAutomatonInfo.size()]);
  }
}
