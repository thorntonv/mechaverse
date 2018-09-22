package org.mechaverse.simulation.common.cellautomaton.simulation.opencl;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLMemory;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;

import java.nio.FloatBuffer;

/**
 * {@link CellularAutomatonSimulator} that uses float values internally.
 */
public class FloatOpenClCellularAutomatonSimulator
    extends AbstractOpenClCellularAutomatonSimulator<FloatBuffer, float[]>
    implements CellularAutomatonSimulator {

  @SuppressWarnings("unused")
  public FloatOpenClCellularAutomatonSimulator(CellularAutomatonSimulatorConfig config) {
    super(config);
  }

  @SuppressWarnings("unused")
  public FloatOpenClCellularAutomatonSimulator(int numAutomata, int automatonInputSize,
                                               int automatonOutputSize, CLDevice device,
                                               CellularAutomatonDescriptor descriptor) {
    super(numAutomata, automatonInputSize, automatonOutputSize, device, descriptor);
  }

  @SuppressWarnings("unused")
  public FloatOpenClCellularAutomatonSimulator(int numAutomata, int automatonInputSize,
                                               int automatonOutputSize,
                                               CellularAutomatonSimulationModel model) {
    super(numAutomata, automatonInputSize, automatonOutputSize, model);
  }

  @SuppressWarnings("unused")
  public FloatOpenClCellularAutomatonSimulator(int numAutomata, int automatonInputSize,
                                               int automatonStateSize, int automatonOutputSize,
                                               long globalWorkSize, long localWorkSize,
                                               CLDevice device, String kernelSource) {
    super(numAutomata, automatonInputSize, automatonStateSize, automatonOutputSize, globalWorkSize,
        localWorkSize, device, kernelSource);
  }

  @Override
  public void getAutomatonState(int index, int[] state) {
    float[] result = new float[state.length];
    getAutomatonState(index, result);
    copyToIntArray(result, state);
  }

  @Override
  public void getAutomataState(int[] state) {
    getAutomataState(toFloatArray(state));
  }

  @Override
  public void setAutomatonState(int index, int[] state) {
    setAutomatonState(index, toFloatArray(state));
  }

  @Override
  public void setAutomataState(int[] state) {
    setAutomataState(toFloatArray(state));
  }

  @Override
  public void setAutomatonInput(int index, int[] input) {
    setAutomatonInput(index, toFloatArray(input));
  }

  @Override
  public void setAutomataInput(int[] input) {
    setAutomataInput(toFloatArray(input));
  }

  @Override
  public void getAutomatonOutput(int index, int[] output) {
    float[] result = new float[output.length];
    getAutomatonOutput(index, result);
    copyToIntArray(result, output);
  }

  @Override
  public void getAutomataOutput(int[] output) {
    float[] result = new float[output.length];
    getAutomataOutput(result);
    copyToIntArray(result, output);
  }

  @Override
  protected CLBuffer<FloatBuffer> createBuffer(int size, CLMemory.Mem... flags) {
    return context.createFloatBuffer(size, flags);
  }

  @Override
  protected void copyFromBufferToArray(FloatBuffer buffer, float[] array, int offset, int length) {
    buffer.get(array, offset, length);
  }

  @Override
  protected void copyFromArrayToBuffer(float[] array, int offset, int length, FloatBuffer buffer) {
    buffer.put(array, offset, length);
  }

  private float[] toFloatArray(int[] array) {
    if (array == null) {
      return null;
    }
    float[] result = new float[array.length];
    for (int idx = 0; idx < array.length; idx++) {
      result[idx] = (float) Math.abs(array[idx]) / Integer.MAX_VALUE;
    }
    return result;
  }

  private void copyToIntArray(float[] src, int[] dest) {
    if (src == null || dest == null) {
      return;
    }
    for (int idx = 0; idx < src.length; idx++) {
      dest[idx] = (int) (src[idx] * Integer.MAX_VALUE);
    }
  }
}
