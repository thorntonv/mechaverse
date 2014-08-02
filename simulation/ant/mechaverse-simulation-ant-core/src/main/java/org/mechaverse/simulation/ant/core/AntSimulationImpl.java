package org.mechaverse.simulation.ant.core;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.simulation.ant.api.SimulationStateUtil;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.SimulationState;
import org.mechaverse.simulation.common.cellautomata.EnvironmentGenerator;

public final class AntSimulationImpl {

  private static final int DEFAULT_ENVIRONMENT_WIDTH = 200;
  private static final int DEFAULT_ENVIRONMENT_HEIGHT = 200;

  private SimulationState state;
  private final List<EnvironmentSimulator> environmentSimulations = new ArrayList<>();
  private final EnvironmentGenerator<CellEnvironment, EntityType> environmentGenerator =
      new AntSimulationEnvironmentGenerator();
  private final ActiveEntityProvider activeEntityProvider;
  private RandomGenerator random = new Well19937c();

  public AntSimulationImpl() {
    this(new SimpleActiveEntityProvider());
  }

  public AntSimulationImpl(ActiveEntityProvider activeEntityProvider) {
    state = new SimulationState();
    state.setIteration(0);
    state.setEnvironment(environmentGenerator.generate(DEFAULT_ENVIRONMENT_WIDTH,
        DEFAULT_ENVIRONMENT_HEIGHT, random).getEnvironment());
    this.activeEntityProvider = activeEntityProvider;
    setState(state);
  }

  public SimulationState getState() {
    for(EnvironmentSimulator environmentSimulation : environmentSimulations) {
      environmentSimulation.updateModel();
    }
    return state;
  }

  public void setState(SimulationState state) {
    this.state = state;

    environmentSimulations.clear();
    for (Environment environment : SimulationStateUtil.getEnvironments(state)) {
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
    state.setSeed(String.valueOf(seed));

    state.setIteration(state.getIteration()+1);
  }
}
