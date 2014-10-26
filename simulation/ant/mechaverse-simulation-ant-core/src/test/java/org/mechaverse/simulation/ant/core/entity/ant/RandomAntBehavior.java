package org.mechaverse.simulation.ant.core.entity.ant;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.core.entity.ant.ActiveAnt.AntBehavior;
import org.mechaverse.simulation.common.util.RandomUtil;

/**
 * An {@link AntBehavior} implementation that occasionally generates completely random outputs.
 */
public class RandomAntBehavior extends SimpleAntBehavior {

  private final AntOutput output = new AntOutput();
  private final int[] data = new int[AntOutput.DATA_SIZE];

  @Override
  public AntOutput getOutput(RandomGenerator random) {
    if(RandomUtil.nextEvent(.5, random)) {
      return super.getOutput(random);
    }
    for (int idx = 0; idx < data.length; idx++) {
      data[idx] = random.nextInt();
    }
    output.setData(data);
    return output;
  }
}
