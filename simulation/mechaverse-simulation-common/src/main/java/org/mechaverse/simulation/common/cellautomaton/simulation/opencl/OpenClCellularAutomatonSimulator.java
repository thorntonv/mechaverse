package org.mechaverse.simulation.common.cellautomaton.simulation.opencl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.IntBuffer;

import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonAllocator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory.Mem;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.CLProgram;

/**
 * Simulates cellular automata using an OpenCL device.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public final class OpenClCellularAutomatonSimulator implements CellularAutomatonSimulator {

  public static final String KERNEL_NAME = "cellautomaton_simulation";

  private static final Logger logger = LoggerFactory
      .getLogger(OpenClCellularAutomatonSimulator.class);

  private final int numAutomata;
  private final int automatonInputSize;
  private final int automatonStateSize;
  private final int automatonOutputSize;
  private final long globalWorkSize;
  private final long localWorkSize;
  private final CellularAutomatonAllocator allocator;

  private final CLContext context;
  private final CLCommandQueue queue;
  private final CLKernel kernel;
  private final CLBuffer<IntBuffer> inputMapBuffer;
  private final CLBuffer<IntBuffer> inputBuffer;
  private final CLBuffer<IntBuffer> stateBuffer;
  private final CLBuffer<IntBuffer> outputMapBuffer;
  private final CLBuffer<IntBuffer> outputBuffer;
  private boolean finished = true;
  private boolean automatonStateUpdated = false;
  private boolean inputMapUpdated = true;
  private boolean outputMapUpdated = false;

  public OpenClCellularAutomatonSimulator(CellularAutomatonSimulatorConfig config) {
    this(config.getNumAutomata(), config.getAutomatonInputSize(), config.getAutomatonOutputSize(),
        CLPlatform.getDefault().getMaxFlopsDevice(),
            new CellularAutomatonSimulationModelBuilder().buildModel(config.getDescriptor()));
  }

  public OpenClCellularAutomatonSimulator(int numAutomata, int automatonInputSize,
      int automatonOutputSize, CLDevice device, CellularAutomatonDescriptor descriptor) {
    this(numAutomata, automatonInputSize, automatonOutputSize, device,
        new CellularAutomatonSimulationModelBuilder().buildModel(descriptor));
  }

  public OpenClCellularAutomatonSimulator(int numAutomata, int automatonInputSize,
      int automatonOutputSize, CellularAutomatonSimulationModel model) {
    this(numAutomata, automatonInputSize, automatonOutputSize,
        CLPlatform.getDefault().getMaxFlopsDevice(), model);
  }

  private OpenClCellularAutomatonSimulator(int numAutomata, int automatonInputSize,
      int automatonOutputSize, CLDevice device, CellularAutomatonSimulationModel model) {
    this(numAutomata, automatonInputSize, model.getStateSize(), automatonOutputSize,
        numAutomata * model.getLogicalUnitCount(), model.getLogicalUnitCount(),
            device, getKernelSource(model));
  }

  public OpenClCellularAutomatonSimulator(int numAutomata, int automatonInputSize,
      int automatonStateSize, int automatonOutputSize, long globalWorkSize, long localWorkSize,
          CLDevice device, String kernelSource) {
    logger.debug("{}(numAutomata = {}, automatonInputSize = {}, "
        + "automatonStateSize = {}, automatonOutputSize = {})", getClass().getSimpleName(),
        numAutomata, automatonInputSize, automatonStateSize, automatonOutputSize);

    this.numAutomata = numAutomata;
    this.automatonInputSize = automatonInputSize;
    this.automatonStateSize = automatonStateSize;
    this.automatonOutputSize = automatonOutputSize;
    this.globalWorkSize = globalWorkSize;
    this.localWorkSize = localWorkSize;
    this.allocator = new CellularAutomatonAllocator(numAutomata);

    this.context = CLContext.create(device);
    CLProgram program = context.createProgram(kernelSource).build();
    this.kernel = program.createCLKernel(KERNEL_NAME);
    this.queue = device.createCommandQueue();

    // Allocate buffers.
    this.inputMapBuffer =
        context.createIntBuffer(numAutomata * Math.max(automatonInputSize, 1), Mem.READ_ONLY);
    this.inputBuffer =
        context.createIntBuffer(numAutomata * Math.max(automatonInputSize, 1), Mem.READ_ONLY);
    this.stateBuffer = context.createIntBuffer(numAutomata * automatonStateSize, Mem.READ_WRITE);
    this.outputMapBuffer =
        context.createIntBuffer(numAutomata * automatonOutputSize, Mem.READ_WRITE);
    this.outputBuffer = context.createIntBuffer(numAutomata * automatonOutputSize, Mem.WRITE_ONLY);

    kernel.putArg(inputMapBuffer)
        .putArg(inputBuffer)
        .putArg(stateBuffer)
        .putArg(outputMapBuffer)
        .putArg(outputBuffer)
        .putArg(automatonInputSize)
        .putArg(automatonOutputSize);

    // Initialize the input/output maps.
    int[] inputMap = new int[automatonInputSize];
    int[] outputMap = new int[automatonOutputSize];
    for (int idx = 0; idx < numAutomata; idx++) {
      setAutomatonInputMap(idx, inputMap);
      setAutomatonOutputMap(idx, outputMap);
    }
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
    return automatonInputSize;
  }

  @Override
  public int getAutomatonStateSize() {
    return automatonStateSize;
  }

  @Override
  public int getAutomatonOutputSize() {
    return automatonOutputSize;
  }

  @Override
  public void getAutomatonState(int index, int[] state) {
    if (!finished) {
      queue.finish();
      finished = true;
    }
    if (automatonStateUpdated) {
      // Finish the write before starting the read.
      queue.putWriteBuffer(stateBuffer, true);
      automatonStateUpdated = false;
    }
    queue.putReadBuffer(stateBuffer, true);
    stateBuffer.getBuffer().position(index * automatonStateSize);
    stateBuffer.getBuffer().get(state);
    stateBuffer.getBuffer().rewind();
  }

  @Override
  public void setAutomatonState(int index, int[] state) {
    if (!finished) {
      queue.finish();
      finished = true;
    }
    stateBuffer.getBuffer().position(index * automatonStateSize);
    stateBuffer.getBuffer().put(state);
    stateBuffer.getBuffer().rewind();
    automatonStateUpdated = true;
  }

  @Override
  public void setAutomatonInputMap(int index, int[] inputMap) {
    for (int idx = 0; idx < inputMap.length; idx++) {
      inputMap[idx] = Math.abs(inputMap[idx]) % automatonStateSize;
    }
    if (!finished) {
      queue.finish();
      finished = true;
    }
    inputMapBuffer.getBuffer().position(index * automatonInputSize);
    inputMapBuffer.getBuffer().put(inputMap);
    inputMapBuffer.getBuffer().rewind();
    inputMapUpdated = true;
  }

  @Override
  public void setAutomatonInput(int index, int[] input) {
    if (!finished) {
      queue.finish();
      finished = true;
    }
    inputBuffer.getBuffer().position(index * automatonInputSize);
    inputBuffer.getBuffer().put(input);
    inputBuffer.getBuffer().rewind();
  }

  @Override
  public void setAutomatonOutputMap(int index, int[] outputMap) {
    for (int idx = 0; idx < outputMap.length; idx++) {
      outputMap[idx] = Math.abs(outputMap[idx]) % automatonStateSize;
    }
    if (!finished) {
      queue.finish();
      finished = true;
    }
    outputMapBuffer.getBuffer().position(index * automatonOutputSize);
    outputMapBuffer.getBuffer().put(outputMap);
    outputMapBuffer.getBuffer().rewind();
    outputMapUpdated = true;
  }

  @Override
  public void getAutomatonOutput(int index, int[] output) {
    if (!finished) {
      queue.finish();
      finished = true;
    }
    outputBuffer.getBuffer().position(index * automatonOutputSize);
    outputBuffer.getBuffer().get(output);
    outputBuffer.getBuffer().rewind();
  }

  @Override
  public void update() {
    if (automatonStateUpdated) {
      queue.putWriteBuffer(stateBuffer, false);
      automatonStateUpdated = false;
    }
    if (inputMapUpdated) {
      queue.putWriteBuffer(inputMapBuffer, false);
      inputMapUpdated = false;
    }
    if (outputMapUpdated) {
      queue.putWriteBuffer(outputMapBuffer, false);
      outputMapUpdated = false;
    }
    queue.putWriteBuffer(inputBuffer, false)
        .put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize)
        .putReadBuffer(outputBuffer, false);
    finished = false;
  }

  @Override
  public void close() throws Exception {
    queue.finish();
    inputBuffer.release();
    stateBuffer.release();
    outputBuffer.release();
    queue.release();
    kernel.release();
    context.release();

    logger.debug("close()");
  }

  private static String getKernelSource(CellularAutomatonSimulationModel model) {
    OpenClCellularAutomatonGeneratorImpl generator =
        new OpenClCellularAutomatonGeneratorImpl(model);
    StringWriter strWriter = new StringWriter();
    PrintWriter out = new PrintWriter(strWriter);
    generator.generate(out);
    return strWriter.toString();
  }
}
