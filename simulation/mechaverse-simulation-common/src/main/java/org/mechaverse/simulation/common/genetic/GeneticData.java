package org.mechaverse.simulation.common.genetic;

import gnu.trove.list.array.TIntArrayList;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Base class for genetic data.
 *
 * @author Vance Thornton <thorntonv@mechaverse.org>
 */
public class GeneticData {

  protected static final int BYTES_PER_INT = Integer.SIZE / Byte.SIZE;

  public static class Builder {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream(512*1024);
    private final DataOutputStream dataOut = new DataOutputStream(out);
    private final TIntArrayList crossoverData = new TIntArrayList(128*1024);

    public Builder write(byte[] data) {
      try {
        dataOut.write(data);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    public Builder writeInt(int value, int group) {
      try {
        dataOut.writeInt(value);

        for (int cnt = 1; cnt <= BYTES_PER_INT; cnt++) {
          crossoverData.add(group);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    public GeneticData build() {
      return new GeneticData(out.toByteArray(), crossoverData.toArray());
    }
  }

  protected final byte[] data;
  protected final int[] crossoverData;

  public static Builder newBuilder() {
    return new Builder();
  }

  protected GeneticData(GeneticData geneticData) {
    this(geneticData.getData(), geneticData.getCrossoverData());
  }

  public GeneticData(byte[] data, int[] crossoverData) {
    this.data = data;
    this.crossoverData = crossoverData;
  }

  public byte[] getData() {
    return data;
  }

  public int[] getCrossoverData() {
    return crossoverData;
  }

  @Override
  public boolean equals(Object otherObject) {
    if (otherObject == null) {
      return false;
    } else if (otherObject == this) {
      return true;
    } else if(!(otherObject instanceof GeneticData)) {
      return false;
    }

    GeneticData otherData = (GeneticData) otherObject;
    return Arrays.equals(data, otherData.data)
        && Arrays.equals(crossoverData, otherData.crossoverData);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(data) + Arrays.hashCode(crossoverData);
  }
}
