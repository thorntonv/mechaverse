package org.mechaverse.simulation.ant.core.entity.ant;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Before;
import org.junit.Test;
import org.mechaverse.simulation.ant.core.AntSimulationTestUtil;

/**
 * A unit test of {@link AntOutputDataOutputStream} and {@link AntOutputDataInputStream}.
 */
public class AntOutputDataStreamTest {

  private RandomGenerator random;

  @Before
  public void setUp() {
    random = new Well19937c(AntOutputDataStreamTest.class.getName().hashCode());
  }

  @Test
  public void writeAndRead() throws IOException {
    AntOutputDataOutputStream out = new AntOutputDataOutputStream();
    List<AntOutput> expectedOutputs = new ArrayList<>();
    for (int cnt = 1; cnt <= 100; cnt++) {
      AntOutput output = AntSimulationTestUtil.randomAntOutput(random);
      out.writeAntOutput(output);
      expectedOutputs.add(output);
    }
    out.close();

    try (AntOutputDataInputStream in = new AntOutputDataInputStream(out.toByteArray())) {
      for (AntOutput expectedOutput : expectedOutputs) {
        AntOutput output = in.readAntOutput();
        assertArrayEquals(expectedOutput.getData(), output.getData());
      }
    }
  }
}
