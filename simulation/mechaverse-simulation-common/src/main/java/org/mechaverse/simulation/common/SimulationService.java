package org.mechaverse.simulation.common;

public interface SimulationService {

  byte[] getState();
  void setState(byte[] state);

  byte[] getModel();
  void setModel(byte[] model);

  void step(int count);
}
