package org.mechaverse.simulation.ant.core.entity.ant;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A {@link DataOutputStream} that can be used to write {@link AntOutput} data.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public final class AntOutputDataOutputStream extends DataOutputStream {

  private static final int DEFAULT_BUFFER_SIZE = 128 * 1024;

  // TODO(thorntonv): Implement unit test for this class.

  private ByteArrayOutputStream out;

  public AntOutputDataOutputStream() {
    this(new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE));
  }

  public AntOutputDataOutputStream(ByteArrayOutputStream out) {
    super(newGZIPOutputStream(out));
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

  private static GZIPOutputStream newGZIPOutputStream(OutputStream out) {
    try {
      return new GZIPOutputStream(out, DEFAULT_BUFFER_SIZE, true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
