package org.mechaverse.simulation.ant.core;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.SimulationModelUtil;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.SimulationModel;
import org.mechaverse.simulation.common.cellautomata.EnvironmentGenerator;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

public final class AntSimulation {

  private static final int DEFAULT_ENVIRONMENT_WIDTH = 200;
  private static final int DEFAULT_ENVIRONMENT_HEIGHT = 200;

  private static final Logger logger = LoggerFactory.getLogger(AntSimulationServiceImpl.class);

  private AntSimulationState state;
  private final List<EnvironmentSimulator> environmentSimulations = new ArrayList<>();
  private final ActiveEntityProvider activeEntityProvider;
  private final RandomGenerator random;

  public static AntSimulationState randomState() throws IOException {
    return randomState(new AntSimulationEnvironmentGenerator(), RandomUtil.newGenerator());
  }

  public static AntSimulationState randomState(
      EnvironmentGenerator<CellEnvironment, EntityType> environmentGenerator,
      RandomGenerator random) {
    return randomState(
        environmentGenerator, DEFAULT_ENVIRONMENT_WIDTH, DEFAULT_ENVIRONMENT_HEIGHT, random);
  }

  public static AntSimulationState randomState(
      EnvironmentGenerator<CellEnvironment, EntityType> environmentGenerator,
      int width, int height, RandomGenerator random) {
    AntSimulationState state = new AntSimulationState();
    state.getModel().setIteration(0);
    state.getModel().setEnvironment(environmentGenerator
        .generate(width, height, random)
        .getEnvironment());
    state.getModel().setSeed(String.valueOf(random.nextLong()));
    return state;
  }

  public AntSimulation() {
    this(new SimpleActiveEntityProvider(), RandomUtil.newGenerator());
  }

  public AntSimulation(ActiveEntityProvider activeEntityProvider, RandomGenerator random) {
    this.activeEntityProvider = activeEntityProvider;
    this.random = random;

    state = randomState(new AntSimulationEnvironmentGenerator(), random);
    setState(state);
  }

  public AntSimulationState getState() {
    state.removeAllEntityValues();
    for (EnvironmentSimulator environmentSimulation : environmentSimulations) {
      environmentSimulation.updateModel();

      for (ActiveEntity activeEntity : environmentSimulation.getActiveEntities()) {
        state.putEntityValues(activeEntity.getEntity(), activeEntity.getState());
      }
    }
    return state;
  }

  public void setState(AntSimulationState state) {
    this.state = state;
    SimulationModel simulationModel = state.getModel();

    environmentSimulations.clear();
    for (Environment environment : SimulationModelUtil.getEnvironments(simulationModel)) {
      environmentSimulations.add(new EnvironmentSimulator(environment, activeEntityProvider));
    }

    for (EnvironmentSimulator environmentSimulation : environmentSimulations) {
      for (ActiveEntity activeEntity : environmentSimulation.getActiveEntities()) {
        activeEntity.setState(state.getEntityValues(activeEntity.getEntity()));
      }
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

    if (logger.isDebugEnabled()) {
      logger.debug("seed = {}", simulationModel.getSeed());
    }

    for (EnvironmentSimulator environmentSimulation : environmentSimulations) {
      environmentSimulation.update(state, random);
    }

    // Set the seed to be used for the next step.
    simulationModel.setSeed(String.valueOf(random.nextLong()));
    simulationModel.setIteration(simulationModel.getIteration()+1);
  }

  @VisibleForTesting
  void addObserver(EntityManager.Observer observer) {
    for (EnvironmentSimulator environmentSimulator : environmentSimulations) {
      environmentSimulator.addObserver(observer);
    }
  }
}
