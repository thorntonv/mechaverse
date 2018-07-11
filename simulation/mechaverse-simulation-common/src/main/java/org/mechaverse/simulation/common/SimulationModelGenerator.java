package org.mechaverse.simulation.common;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.model.SimulationModel;

public interface SimulationModelGenerator<SIM_MODEL extends SimulationModel> {

  SIM_MODEL generate(RandomGenerator random);
}
