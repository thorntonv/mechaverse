package org.mechaverse.simulation.common;

/**
 * A key that identifies a simulation state.
 */
public final class SimulationStateKey {

  private final String simulationId;
  private final String instanceId;
  private final long iteration;

  public SimulationStateKey(String simulationId, String instanceId, long iteration) {
    super();
    this.simulationId = simulationId;
    this.instanceId = instanceId;
    this.iteration = iteration;
  }

  public String getSimulationId() {
    return simulationId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public long getIteration() {
    return iteration;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((instanceId == null) ? 0 : instanceId.hashCode());
    result = prime * result + (int) (iteration ^ (iteration >>> 32));
    result = prime * result + ((simulationId == null) ? 0 : simulationId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    SimulationStateKey other = (SimulationStateKey) obj;
    if (instanceId == null) {
      if (other.instanceId != null) return false;
    } else if (!instanceId.equals(other.instanceId)) return false;
    if (iteration != other.iteration) return false;
    if (simulationId == null) {
      if (other.simulationId != null) return false;
    } else if (!simulationId.equals(other.simulationId)) return false;
    return true;
  }

  @Override
  public String toString() {
    return simulationId + "-" + instanceId + "-" + iteration;
  }
}
