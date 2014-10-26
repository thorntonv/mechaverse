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

  // TODO(thorntonv): Implement unit test for this class.

  public static class Builder {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final DataOutputStream dataOut = new DataOutputStream(out);
    private final TIntArrayList crossoverPoints = new TIntArrayList();

    public Builder write(byte[] data) {
      try {
        dataOut.write(data);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    public Builder writeInt(int value) {
      try {
        dataOut.writeInt(value);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    /**
     * Marks the current position as a crossover point.
     */
    public Builder markCrossoverPoint() {
      crossoverPoints.add(out.size());
      return this;
    }

    /**
     * Adds a position to the list of crossover points.
     */
    public Builder addCrossoverPoint(int position) {
      crossoverPoints.add(position);
      return this;
    }

    public GeneticData build() {
      return new GeneticData(out.toByteArray(), crossoverPoints.toArray());
    }
  }

  protected final byte[] data;
  protected final int[] crossoverPoints;

  public static Builder newBuilder() {
    return new Builder();
  }

  public GeneticData(byte[] data, int[] crossoverPoints) {
    this.data = data;
    this.crossoverPoints = crossoverPoints;
  }

  public byte[] getData() {
    return data;
  }

  public int[] getCrossoverPoints() {
    return crossoverPoints;
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
        && Arrays.equals(crossoverPoints, otherData.crossoverPoints);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(data) + Arrays.hashCode(crossoverPoints);
  }
}
