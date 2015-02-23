package org.mechaverse.simulation.common.simple;

import com.google.common.base.Function;

/**
 * A base class for simple simulation models.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class SimpleSimulationModel {
  
  public static final Function<SimpleSimulationModel, byte[]> SERIALIZER =
      new SimpleSimulationModelSerializer();

  public static class SimpleSimulationModelSerializer 
      implements Function<SimpleSimulationModel, byte[]> {
    @Override
    public byte[] apply(SimpleSimulationModel model) {
      return new byte[0];
    }
  }
}
