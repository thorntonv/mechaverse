package org.mechaverse.simulation.ant.core.entity.ant;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * A {@link DataInputStream} for reading {@link AntOutput} data.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public final class AntOutputDataInputStream extends DataInputStream {

  private static final int BUFFER_SIZE = 128 * 1024;

  // TODO(thorntonv): Implement unit test for this class.

  private final AntOutput antOutput = new AntOutput();
  private final int[] antOutputData = new int[AntOutput.DATA_SIZE];

  public AntOutputDataInputStream(byte[] data) {
    this(new ByteArrayInputStream(data));
  }

  public AntOutputDataInputStream(InputStream in) {
    super(newGZIPInputStream(in));
  }

  public AntOutput readAntOutput() throws IOException {
    for (int idx = 0; idx < antOutputData.length; idx++) {
      antOutputData[idx] = readInt();
    }
    antOutput.setData(antOutputData);
    return antOutput;
  }

  private static final GZIPInputStream newGZIPInputStream(InputStream in) {
    try {
      return new GZIPInputStream(in, BUFFER_SIZE);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
