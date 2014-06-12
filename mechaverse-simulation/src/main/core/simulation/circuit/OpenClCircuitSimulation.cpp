#include "OpenClCircuitSimulation.h"

#include <CL/cl.h>
#include <cstddef>
#include <iostream>
#include <iterator>
#include <string>
#include <utility>
#include <vector>

#define __CL_ENABLE_EXCEPTIONS

using namespace cl;
using namespace std;
using namespace mechaverse;

OpenClCircuitSimulation::OpenClCircuitSimulation(
    cl::Context& context, cl::Device& device, std::string kernelSource) {
  this->context = context;

  vector<Device> devices;
  devices.push_back(device);

  queue = CommandQueue(context, device);

  Program::Sources source(1, std::make_pair(kernelSource.c_str(), kernelSource.length() + 1));
  Program program = Program(context, source);
  program.build(devices);

  kernel = Kernel(program, "circuit_simulation");

  setState(NULL, 0);
}

void OpenClCircuitSimulation::getState(char* state, std::size_t size) {
  queue.enqueueReadBuffer(stateBuffer, CL_TRUE, 0, size, state);
}

void OpenClCircuitSimulation::setState(char* state, std::size_t size) {
  stateBuffer = Buffer(context, CL_MEM_READ_WRITE, size);
  queue.enqueueWriteBuffer(stateBuffer, CL_TRUE, 0, size, state);
}

void OpenClCircuitSimulation::setInput(char* input, std::size_t size) {
  inputBuffer = Buffer(context, CL_MEM_WRITE_ONLY, size);
  queue.enqueueWriteBuffer(inputBuffer, CL_TRUE, 0, size, input);
}

void OpenClCircuitSimulation::getOutput(char* output, std::size_t size) {
  queue.enqueueReadBuffer(outputBuffer, CL_TRUE, 0, size, output);
}

void OpenClCircuitSimulation::update() {
  kernel.setArg(0, inputBuffer);
  kernel.setArg(1, outputBuffer);
  kernel.setArg(2, stateBuffer);

  NDRange global(1);
  NDRange local(1);
  queue.enqueueNDRangeKernel(kernel, NullRange, global, local);
}

OpenClCircuitSimulation::~OpenClCircuitSimulation() {
}
