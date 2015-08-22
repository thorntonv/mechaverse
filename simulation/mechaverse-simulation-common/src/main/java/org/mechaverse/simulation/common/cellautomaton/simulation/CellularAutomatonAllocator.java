package org.mechaverse.simulation.common.cellautomaton.simulation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Used to track the cellular automaton instances that are being used.
 */
public class CellularAutomatonAllocator {

  private final Set<Integer> availableInstances = new HashSet<>();

  public CellularAutomatonAllocator(int numInstances) {
    for (int idx = 0; idx < numInstances; idx++) {
      availableInstances.add(idx);
    }
  }

  public int getAvailableCount() {
    return availableInstances.size();
  }

  public int allocate() {
    Iterator<Integer> it = availableInstances.iterator();
    if (it.hasNext()) {
      int idx = it.next();
      it.remove();
      return idx;
    }
    throw new IllegalStateException("No instances are available");
  }

  public void deallocate(int idx) {
    availableInstances.add(idx);
  }
}
