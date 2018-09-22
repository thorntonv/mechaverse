package org.mechaverse.simulation.common.cellautomaton.simulation;

import gnu.trove.stack.array.TIntArrayStack;

/**
 * Used to track the cellular automaton instances that are being used.
 */
public class CellularAutomatonAllocator {

  private final TIntArrayStack availableInstances;

  public CellularAutomatonAllocator(int numInstances) {
    availableInstances = new TIntArrayStack(numInstances);
    for (int idx = 0; idx < numInstances; idx++) {
      availableInstances.push(numInstances - idx - 1);
    }
  }

  public int getAvailableCount() {
    return availableInstances.size();
  }

  public int allocate() {
    if(availableInstances.size() == 0) {
      throw new IllegalStateException("No instances are available");
    }
    return availableInstances.pop();
  }

  public void deallocate(int idx) {
    availableInstances.push(idx);
  }
}
