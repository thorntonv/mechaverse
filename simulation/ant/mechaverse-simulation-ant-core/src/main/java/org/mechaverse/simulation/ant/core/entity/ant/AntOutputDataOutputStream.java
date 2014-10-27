package org.mechaverse.simulation.ant.core.entity.ant;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A {@link DataOutputStream} that can be used to write {@link AntOutput} data.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public final class AntOutputDataOutputStream extends DataOutputStream {

  // TODO(thorntonv): Implement unit test for this class.

  private ByteArrayOutputStream out;

  public AntOutputDataOutputStream() {
    this(new ByteArrayOutputStream(128 * 1024));
  }

  public AntOutputDataOutputStream(ByteArrayOutputStream out) {
    super(out);
    this.out = out;
  }

  public void writeAntOutput(AntOutput antOutput) throws IOException {
    int[] antOutputData = antOutput.getData();
    for (int idx = 0; idx < antOutputData.length; idx++) {
      writeInt(antOutputData[idx]);
    }
  }

  public byte[] toByteArray() {
    return out.toByteArray();
  }
}
