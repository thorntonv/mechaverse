package org.mechaverse.simulation.common.circuit;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Used to track the circuits that are being used.
 */
public class CircuitAllocator {

  private final Set<Integer> availableCircuits = new HashSet<Integer>();

  public CircuitAllocator(int numCircuits) {
    for (int circuitIndex = 0; circuitIndex < numCircuits; circuitIndex++) {
      availableCircuits.add(circuitIndex);
    }
  }

  public int getAvailableCircuitCount() {
    return availableCircuits.size();
  }

  public int allocateCircuit() {
    Iterator<Integer> it = availableCircuits.iterator();
    if (it.hasNext()) {
      int circuitIndex = it.next();
      it.remove();
      return circuitIndex;
    }
    throw new IllegalStateException("No circuits are available");
  }

  public void deallocateCircuit(int circuitIndex) {
    availableCircuits.add(circuitIndex);
  }
}
