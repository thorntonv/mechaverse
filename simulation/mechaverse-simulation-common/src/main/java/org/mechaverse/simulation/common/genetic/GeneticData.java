package org.mechaverse.simulation.common.genetic;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import gnu.trove.list.array.TIntArrayList;

/**
 * Base class for genetic data.
 *
 * @author Vance Thornton <thorntonv@mechaverse.org>
 */
public class GeneticData {

  protected static final int BYTES_PER_INT = Integer.SIZE / Byte.SIZE;

  public static class Builder {

    private final ByteArrayOutputStream byteOut = new ByteArrayOutputStream(16 * 1024);
    private final DataOutputStream out = new DataOutputStream(byteOut);
    private final TIntArrayList crossoverGroups = new TIntArrayList(16 * 1024);
    private final TIntArrayList crossoverSplitPoints = new TIntArrayList(4 * 1024);

    public Builder write(byte data, int group) {
      try {
        out.writeByte(data);
        crossoverGroups.add(group);
        return this;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    public Builder write(byte[] data, int group) {
      try {
        out.write(data);
        for (int cnt = 1; cnt <= data.length; cnt++) {
          crossoverGroups.add(group);
        }
        return this;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    public Builder writeInt(int value, int group) {
      try {
        out.writeInt(value);
        for (int cnt = 1; cnt <= BYTES_PER_INT; cnt++) {
          crossoverGroups.add(group);
        }
        return this;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    public Builder markSplitPoint() {
      crossoverSplitPoints.add(out.size());
      return this;
    }

    public GeneticData build() {
      return new GeneticData(byteOut.toByteArray(), crossoverGroups.toArray(),
          crossoverSplitPoints.toArray());
    }
  }

  protected final byte[] data;
  protected final int[] crossoverGroups;
  protected final int[] crossoverSplitPoints;

  public static Builder newBuilder() {
    return new Builder();
  }

  @SuppressWarnings("CopyConstructorMissesField")
  protected GeneticData(GeneticData geneticData) {
    this(geneticData.getData(), geneticData.getCrossoverGroups(),
        geneticData.getCrossoverSplitPoints());
  }

  public GeneticData(byte[] data, int[] crossoverGroups, int[] crossoverSplitPoints) {
    this.data = data;
    this.crossoverGroups = crossoverGroups;
    this.crossoverSplitPoints = crossoverSplitPoints;
  }

  public byte[] getData() {
    return data;
  }

  public int[] getCrossoverGroups() {
    return crossoverGroups;
  }

  public int[] getCrossoverSplitPoints() {
    return crossoverSplitPoints;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    GeneticData other = (GeneticData) obj;
    if (!Arrays.equals(crossoverGroups, other.crossoverGroups)) {
      return false;
    }
    return Arrays.equals(crossoverSplitPoints, other.crossoverSplitPoints)
        && Arrays.equals(data, other.data);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(crossoverGroups);
    result = prime * result + Arrays.hashCode(crossoverSplitPoints);
    result = prime * result + Arrays.hashCode(data);
    return result;
  }
}
