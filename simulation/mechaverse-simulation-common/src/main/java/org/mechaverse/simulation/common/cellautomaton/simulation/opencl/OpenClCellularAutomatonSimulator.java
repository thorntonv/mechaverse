package org.mechaverse.simulation.common.cellautomaton.simulation.opencl;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLMemory.Mem;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;

import java.nio.IntBuffer;

/**
 * Simulates cellular automata using an OpenCL device.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public final class OpenClCellularAutomatonSimulator
    extends AbstractOpenClCellularAutomatonSimulator<IntBuffer, int[]>
    implements CellularAutomatonSimulator {

  public OpenClCellularAutomatonSimulator(CellularAutomatonSimulatorConfig config) {
    super(config);
  }

  public OpenClCellularAutomatonSimulator(int numAutomata, int automatonInputSize,
                                          int automatonOutputSize, CLDevice device,
                                          CellularAutomatonDescriptor descriptor) {
    super(numAutomata, automatonInputSize, automatonOutputSize, device, descriptor);
  }

  public OpenClCellularAutomatonSimulator(int numAutomata, int automatonInputSize,
                                          int automatonOutputSize,
                                          CellularAutomatonSimulationModel model) {
    super(numAutomata, automatonInputSize, automatonOutputSize, model);
  }

  public OpenClCellularAutomatonSimulator(int numAutomata, int automatonInputSize,
                                          int automatonStateSize, int automatonOutputSize,
                                          long globalWorkSize, long localWorkSize, CLDevice device,
                                          String kernelSource) {
    super(numAutomata, automatonInputSize, automatonStateSize, automatonOutputSize, globalWorkSize,
        localWorkSize, device, kernelSource);
  }

  @Override
  protected CLBuffer<IntBuffer> createBuffer(int size, Mem... flags) {
    return context.createIntBuffer(size, flags);
  }

  @Override
  protected void copyFromBufferToArray(IntBuffer buffer, int[] array, int offset, int length) {
    buffer.get(array, offset, length);
  }

  @Override
  protected void copyFromArrayToBuffer(int[] array, int offset, int length, IntBuffer buffer) {
    buffer.put(array, offset, length);
  }
}
