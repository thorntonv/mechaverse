/*
 * CircuitSimulation.h
 *
 *  Created on: Jun 8, 2014
 *      Author: thorntonv
 */

#ifndef OPENCL_CIRCUITSIMULATION_H_
#define OPENCL_CIRCUITSIMULATION_H_

#include <CL/cl.hpp>
#include <stdlib.h>

namespace mechaverse {

class OpenClCircuitSimulation {

 private:

  cl::Kernel kernel;
  cl::Context context;
  cl::CommandQueue queue;
  cl::Buffer inputBuffer;
  cl::Buffer outputBuffer;
  cl::Buffer stateBuffer;

 public:

  OpenClCircuitSimulation(cl::Context& context, cl::Device& device, std::string kernelSource);
  virtual ~OpenClCircuitSimulation();

  void getState(char* state, size_t size);
  void setState(char* state, size_t size);
  void setInput(char* input, size_t size);
  void getOutput(char* output, size_t size);
  void update();
};

}

#endif
