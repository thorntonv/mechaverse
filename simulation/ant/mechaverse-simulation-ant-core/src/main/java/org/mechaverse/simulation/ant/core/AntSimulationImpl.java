package org.mechaverse.simulation.ant.core;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.simulation.ant.api.SimulationModelUtil;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.SimulationModel;
import org.mechaverse.simulation.common.cellautomata.EnvironmentGenerator;

public final class AntSimulationImpl {

  private static final int DEFAULT_ENVIRONMENT_WIDTH = 200;
  private static final int DEFAULT_ENVIRONMENT_HEIGHT = 200;

  private SimulationModel model;
  private final List<EnvironmentSimulator> environmentSimulations = new ArrayList<>();
  private final EnvironmentGenerator<CellEnvironment, EntityType> environmentGenerator =
      new AntSimulationEnvironmentGenerator();
  private final ActiveEntityProvider activeEntityProvider;
  private final RandomGenerator random;

  public AntSimulationImpl() {
    this(new SimpleActiveEntityProvider(), new Well19937c());
  }

  public AntSimulationImpl(ActiveEntityProvider activeEntityProvider, RandomGenerator random) {
    this.activeEntityProvider = activeEntityProvider;
    this.random = random;

    model = new SimulationModel();
    model.setIteration(0);
    model.setEnvironment(environmentGenerator.generate(DEFAULT_ENVIRONMENT_WIDTH,
        DEFAULT_ENVIRONMENT_HEIGHT, random).getEnvironment());
    setState(model);
  }

  public SimulationModel getState() {
    for(EnvironmentSimulator environmentSimulation : environmentSimulations) {
      environmentSimulation.updateModel();
    }
    return model;
  }

  public void setState(SimulationModel state) {
    this.model = state;

    environmentSimulations.clear();
    for (Environment environment : SimulationModelUtil.getEnvironments(state)) {
      environmentSimulations.add(new EnvironmentSimulator(environment, activeEntityProvider));
    }

    long seed = new SecureRandom().nextLong();
    if (state.getSeed() != null) {
      seed = Long.valueOf(state.getSeed());
    } else {
      state.setSeed(String.valueOf(seed));
    }
    random.setSeed(seed);
  }

  public void step() {
    for (EnvironmentSimulator environmentSimulation : environmentSimulations) {
      environmentSimulation.update(random);
    }

    // Set the seed to be used for the next step.
    long seed = random.nextLong();
    random.setSeed(seed);
    model.setSeed(String.valueOf(seed));

    model.setIteration(model.getIteration()+1);
  }
}
