package org.mechaverse.simulation.ant.core.entity.ant;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link DataInputStream} for reading {@link AntOutput} data.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public final class AntOutputDataInputStream extends DataInputStream {

  private final AntOutput antOutput = new AntOutput();
  private final int[] antOutputData = new int[AntOutput.DATA_SIZE];

  public AntOutputDataInputStream(byte[] data) {
    this(new ByteArrayInputStream(data));
  }

  public AntOutputDataInputStream(InputStream in) {
    super(in);
  }

  public AntOutput readAntOutput() throws IOException {
    for (int idx = 0; idx < antOutputData.length; idx++) {
      antOutputData[idx] = readShort();
    }
    antOutput.setData(antOutputData);
    return antOutput;
  }
}
