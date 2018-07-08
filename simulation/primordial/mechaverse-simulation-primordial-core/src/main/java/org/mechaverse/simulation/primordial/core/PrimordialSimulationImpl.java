package org.mechaverse.simulation.primordial.core;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.EntityManager;
import org.mechaverse.simulation.common.util.SimulationModelUtil;
import org.mechaverse.simulation.primordial.core.model.EntityType;
import org.mechaverse.simulation.common.model.Environment;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.common.Simulation;
import org.mechaverse.simulation.common.cellautomaton.EnvironmentGenerator;
import org.mechaverse.simulation.common.cellautomaton.simulation.opencl.DeviceUtil;
import org.mechaverse.simulation.common.datastore.SimulationDataStore;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;

@SuppressWarnings("unused")
public final class PrimordialSimulationImpl implements Simulation {

  private static final int DEFAULT_ENVIRONMENT_WIDTH = 175;
  private static final int DEFAULT_ENVIRONMENT_HEIGHT = 175;

  private static final Logger logger = LoggerFactory.getLogger(PrimordialSimulationImpl.class);

  @Autowired private EnvironmentSimulator.Factory environmentSimulatorFactory;

  private PrimordialSimulationState state;
  private final List<EnvironmentSimulator> environmentSimulations = new ArrayList<>();
  private final RandomGenerator random;

  public static PrimordialSimulationState randomState() {
    return randomState(new PrimordialSimulationEnvironmentGenerator(), RandomUtil.newGenerator());
  }

  public static PrimordialSimulationState randomState(
      EnvironmentGenerator<CellEnvironment, EntityType> environmentGenerator,
      RandomGenerator random) {
    return randomState(
        environmentGenerator, DEFAULT_ENVIRONMENT_WIDTH, DEFAULT_ENVIRONMENT_HEIGHT, random);
  }

  public static PrimordialSimulationState randomState(
      EnvironmentGenerator<CellEnvironment, EntityType> environmentGenerator,
      int width, int height, RandomGenerator random) {
    PrimordialSimulationState state = new PrimordialSimulationState();
    state.getModel().setIteration(0);
    state.getModel().setEnvironment(environmentGenerator
        .generate(width, height, random)
        .getEnvironment());
    state.getModel().setSeed(String.valueOf(random.nextLong()));
    return state;
  }

  public PrimordialSimulationImpl() {
    this(RandomUtil.newGenerator());
  }

  public PrimordialSimulationImpl(RandomGenerator random) {
    this.random = random;
  }

  @Override
  public PrimordialSimulationState getState() {
    for (EnvironmentSimulator environmentSimulation : environmentSimulations) {
      environmentSimulation.updateState(state);
    }
    return state;
  }

  @Override
  public void setState(SimulationDataStore stateDataStore) throws IOException {
    setState(new PrimordialSimulationState(stateDataStore));
  }

  public void setState(PrimordialSimulationState state) {
    this.state = state;
    SimulationModel simulationModel = state.getModel();

    // Close the existing environment simulations.
    for (EnvironmentSimulator environmentSimulation : environmentSimulations) {
      environmentSimulation.close();
    }

    environmentSimulations.clear();
    for (Environment environment : SimulationModelUtil.getEnvironments(simulationModel)) {
      environmentSimulations.add(environmentSimulatorFactory.create(environment.getId()));
    }

    for (EnvironmentSimulator environmentSimulation : environmentSimulations) {
      environmentSimulation.setState(state);
    }
  }

  public void step() {
    SimulationModel simulationModel = state.getModel();

    if (logger.isDebugEnabled()) {
      logger.debug("Performing iteration {}", simulationModel.getIteration());
    }

    if (simulationModel.getSeed() == null) {
      simulationModel.setSeed(String.valueOf(new SecureRandom().nextLong()));
    }

    random.setSeed(Long.valueOf(simulationModel.getSeed()));

    for (EnvironmentSimulator environmentSimulation : environmentSimulations) {
      environmentSimulation.update(state, random);
    }

    if (logger.isDebugEnabled()) {
      // Print seed after updating environments in case seed is changed.
      logger.debug("seed = {}", simulationModel.getSeed());
    }

    // Set the seed to be used for the next step.
    simulationModel.setSeed(String.valueOf(random.nextLong()));
    simulationModel.setIteration(simulationModel.getIteration()+1);
  }

  public List<EnvironmentSimulator> getEnvironmentSimulations() {
    return environmentSimulations;
  }

  @VisibleForTesting
  void addObserver(EntityManager.Observer<SimulationModel, PrimordialSimulationState> observer) {
    for (EnvironmentSimulator environmentSimulator : environmentSimulations) {
      environmentSimulator.addObserver(observer);
    }
  }

  @Override
  public SimulationDataStore generateRandomState() {
    return PrimordialSimulationImpl.randomState();
  }

  @Override
  public void step(int stepCount) {
    for(int cnt = 1; cnt <= stepCount; cnt++) {
      step();
    }
  }

  @Override
  public void step(int stepCount, double targetFitness) {
    step(stepCount);
  }

  @Override
  public String getDeviceInfo() {
    return DeviceUtil.getDeviceInfo();
  }
}
