package org.mechaverse.tools.circuit.generator.java;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.tools.circuit.CircuitTestUtil;
import org.mechaverse.tools.util.compiler.JavaCompilerUtil.CompileException;

public class JavaCircuitCompilerTest {

  @Test
  public void testCompile() throws CompileException {
    Circuit circuit1 = CircuitTestUtil.createTestCircuit1();
    JavaCircuitCompiler circuitCompiler = new JavaCircuitCompiler();

    CircuitSimulation circuitSimulation = circuitCompiler.compile(circuit1);
    int[] state = new int[18];

    state[3] = 0b101; // e2_out1
    state[7] = 0b110; // e1_out1_input3Mask

    circuitSimulation.update(0, state);
    assertEquals(0b100, state[0]); // e1_out1
  }
}
