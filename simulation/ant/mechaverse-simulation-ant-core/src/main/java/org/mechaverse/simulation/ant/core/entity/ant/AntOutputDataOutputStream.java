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

  private static final int DEFAULT_BUFFER_SIZE = 128 * 1024;

  private ByteArrayOutputStream out;

  public AntOutputDataOutputStream() {
    this(new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE));
  }

  public AntOutputDataOutputStream(ByteArrayOutputStream out) {
    super(out);
    this.out = out;
  }

  public void writeAntOutput(AntOutput antOutput) throws IOException {
    int[] antOutputData = antOutput.getData();
    for (int anAntOutputData : antOutputData) {
      writeShort(anAntOutputData);
    }
  }

  public byte[] toByteArray() {
    return out.toByteArray();
  }
}
