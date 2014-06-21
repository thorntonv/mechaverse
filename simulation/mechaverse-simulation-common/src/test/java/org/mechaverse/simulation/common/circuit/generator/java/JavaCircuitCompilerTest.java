package org.mechaverse.simulation.common.circuit.generator.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.simulation.common.circuit.CircuitTestUtil;
import org.mechaverse.simulation.common.util.compiler.JavaCompilerUtil.CompileException;

public class JavaCircuitCompilerTest {

  private static final Circuit CIRCUIT1 = CircuitTestUtil.createTestCircuit(1, 1,
    CircuitTestUtil.createRouting3in3OutElementType(), 1, 2);

  @Test
  public void testCompile() {
    try {
      JavaCircuitCompiler circuitCompiler = new JavaCircuitCompiler();
      CircuitSimulation circuitSimulation = circuitCompiler.compile(CIRCUIT1);

      // e1_out1 = (ex_in2 & e1_out1_input2Mask) | (e2_out1 & e1_out1_input3Mask);
      int[] state = new int[18];
      state[1] = 0b001; // ex_in2
      state[3] = 0b111; // e1_out1_input2Mask
      state[9] = 0b101; // e2_out1
      state[4] = 0b110; // e1_out1_input3Mask
      circuitSimulation.update(0, state);
      assertEquals(0b101, state[0]); // e1_out1
    } catch (CompileException ex) {
      fail(ex.getDiagnostics().toString());
    }
  }
}