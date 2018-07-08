package org.mechaverse.simulation.common.cellautomaton.simulation.opencl;

import com.jogamp.opencl.*;
import com.jogamp.opencl.CLMemory.Mem;
import org.mechaverse.cellautomaton.model.CellularAutomatonDescriptor;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonAllocator;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomatonSimulatorConfig;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.Buffer;
import java.nio.IntBuffer;

/**
 * Simulates cellular automata using an OpenCL device.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public abstract class AbstractOpenClCellularAutomatonSimulator<B extends Buffer, T> {

  public static final String KERNEL_NAME = "cellautomaton_simulation";

  private static final Logger logger = LoggerFactory
      .getLogger(AbstractOpenClCellularAutomatonSimulator.class);

  private final int numAutomata;
  private final int automatonInputSize;
  private final int automatonStateSize;
  private final int automatonOutputSize;
  private final long globalWorkSize;
  private final long localWorkSize;
  private final CellularAutomatonAllocator allocator;

  protected final CLContext context;
  private final CLCommandQueue queue;
  private final CLKernel kernel;
  private final CLBuffer<IntBuffer> inputMapBuffer;
  private final CLBuffer<B> inputBuffer;
  private final CLBuffer<B> stateBuffer;
  private final CLBuffer<IntBuffer> outputMapBuffer;
  private final CLBuffer<B> outputBuffer;
  private boolean finished = true;
  private boolean automatonStateUpdated = false;
  private boolean inputMapUpdated = true;
  private boolean outputMapUpdated = false;

  public AbstractOpenClCellularAutomatonSimulator(CellularAutomatonSimulatorConfig config) {
    this(config.getNumAutomata(), config.getAutomatonInputSize(), config.getAutomatonOutputSize(),
        CLPlatform.getDefault().getMaxFlopsDevice(),
            new CellularAutomatonSimulationModelBuilder().buildModel(config.getDescriptor()));
  }

  public AbstractOpenClCellularAutomatonSimulator(int numAutomata, int automatonInputSize,
                                                  int automatonOutputSize, CLDevice device, CellularAutomatonDescriptor descriptor) {
    this(numAutomata, automatonInputSize, automatonOutputSize, device,
        new CellularAutomatonSimulationModelBuilder().buildModel(descriptor));
  }

  public AbstractOpenClCellularAutomatonSimulator(int numAutomata, int automatonInputSize,
                                                  int automatonOutputSize, CellularAutomatonSimulationModel model) {
    this(numAutomata, automatonInputSize, automatonOutputSize,
        CLPlatform.getDefault().getMaxFlopsDevice(), model);
  }

  private AbstractOpenClCellularAutomatonSimulator(int numAutomata, int automatonInputSize,
                                                   int automatonOutputSize, CLDevice device, CellularAutomatonSimulationModel model) {
    this(numAutomata, automatonInputSize, model.getStateSize(), automatonOutputSize,
        numAutomata * model.getLogicalUnitCount(), model.getLogicalUnitCount(),
            device, getKernelSource(model));
  }

  public AbstractOpenClCellularAutomatonSimulator(int numAutomata, int automatonInputSize,
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
        createBuffer(numAutomata * Math.max(automatonInputSize, 1), Mem.READ_ONLY);
    this.stateBuffer = createBuffer(numAutomata * automatonStateSize, Mem.READ_WRITE);
    this.outputMapBuffer =
        context.createIntBuffer(numAutomata * automatonOutputSize, Mem.READ_WRITE);
    this.outputBuffer = createBuffer(numAutomata * automatonOutputSize, Mem.WRITE_ONLY);

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

  protected abstract CLBuffer<B> createBuffer(final int size, final Mem... flags);
  protected abstract void copyFromBufferToArray(B buffer, T array);
  protected abstract void copyFromArrayToBuffer(T array, B buffer);

  public CellularAutomatonAllocator getAllocator() {
    return allocator;
  }

  public int size() {
    return numAutomata;
  }

  public int getAutomatonInputSize() {
    return automatonInputSize;
  }

  public int getAutomatonStateSize() {
    return automatonStateSize;
  }

  public int getAutomatonOutputSize() {
    return automatonOutputSize;
  }

  public void getAutomatonState(int index, T state) {
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
    copyFromBufferToArray(stateBuffer.getBuffer(), state);
    stateBuffer.getBuffer().rewind();
  }

  public void setAutomatonState(int index, T state) {
    if (!finished) {
      queue.finish();
      finished = true;
    }
    stateBuffer.getBuffer().position(index * automatonStateSize);
    copyFromArrayToBuffer(state, stateBuffer.getBuffer());
    stateBuffer.getBuffer().rewind();
    automatonStateUpdated = true;
  }

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

  public void setAutomatonInput(int index, T input) {
    if (!finished) {
      queue.finish();
      finished = true;
    }
    inputBuffer.getBuffer().position(index * automatonInputSize);
    copyFromArrayToBuffer(input, inputBuffer.getBuffer());
    inputBuffer.getBuffer().rewind();
  }

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

  public void getAutomatonOutput(int index, T output) {
    if (!finished) {
      queue.finish();
      finished = true;
    }
    outputBuffer.getBuffer().position(index * automatonOutputSize);
    copyFromBufferToArray(outputBuffer.getBuffer(), output);
    outputBuffer.getBuffer().rewind();
  }

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

  public void close() {
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
