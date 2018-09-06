package org.mechaverse.simulation.common.cellautomaton.simulation;

public final class BitwiseCellularAutomatonSimulatorAdapter implements CellularAutomatonSimulator {

  private final int bitsPerEntity;
  private final int numAutomata;
  private final CellularAutomatonSimulator simulator;
  private final CellularAutomatonAllocator allocator;

  public BitwiseCellularAutomatonSimulatorAdapter(CellularAutomatonSimulator simulator,
      int bitsPerEntity) {
    this.numAutomata = simulator.size() * Integer.SIZE / bitsPerEntity;
    this.simulator = simulator;
    this.allocator = new CellularAutomatonAllocator(numAutomata);
    this.bitsPerEntity = bitsPerEntity;
  }

  @Override
  public CellularAutomatonAllocator getAllocator() {
    return allocator;
  }

  @Override
  public int size() {
    return numAutomata;
  }

  @Override
  public int getAutomatonInputSize() {
    return simulator.getAutomatonInputSize();
  }

  @Override
  public int getAutomatonStateSize() {
    return simulator.getAutomatonStateSize();
  }

  @Override
  public int getAutomatonOutputSize() {
    return simulator.getAutomatonOutputSize();
  }

  @Override
  public void getAutomatonState(int index, int[] state) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void getAutomataState(int[] state) {
    simulator.getAutomataState(state);
  }

  @Override
  public void setAutomatonState(int index, int[] state) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAutomataState(int[] state) {
    simulator.setAutomataState(state);
  }

  @Override
  public void setAutomatonInputMap(int index, int[] inputMap) {
    simulator.setAutomatonInputMap(index / (Integer.SIZE / bitsPerEntity), inputMap);
  }

  @Override
  public void setAutomatonInput(int index, int[] input) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAutomataInput(int[] input) {
    simulator.setAutomataInput(input);
  }

  @Override
  public void setAutomatonOutputMap(int index, int[] outputMap) {
    simulator.setAutomatonOutputMap(index / (Integer.SIZE / bitsPerEntity), outputMap);
  }

  @Override
  public void getAutomatonOutput(int index, int[] output) {
    simulator.getAutomatonOutput(index, output);
  }

  @Override
  public void getAutomataOutput(int[] output) {
    simulator.getAutomataOutput(output);
  }

  @Override
  public void update() {
    simulator.update();
  }

  @Override
  public void close() throws Exception {
    simulator.close();
  }
}
