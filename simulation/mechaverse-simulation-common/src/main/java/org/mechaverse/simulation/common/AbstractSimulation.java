package org.mechaverse.simulation.common;

import com.google.common.base.Preconditions;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class AbstractSimulation<
    SIM_MODEL extends SimulationModel<ENV_MODEL, ENT_MODEL, ENT_TYPE>,
    ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
    ENT_MODEL extends EntityModel<ENT_TYPE>,
    ENT_TYPE extends Enum<ENT_TYPE>> implements Simulation<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> {

  private static final int DEFAULT_ENVIRONMENT_WIDTH = 175;
  private static final int DEFAULT_ENVIRONMENT_HEIGHT = 175;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final SimulationModelGenerator<SIM_MODEL> simulationModelGenerator;
  private final EnvironmentFactory<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> environmentFactory;

  private SIM_MODEL model;
  private final List<Environment<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE>> environments = new ArrayList<>();
  private final RandomGenerator random;


  public AbstractSimulation(
      SimulationModelGenerator<SIM_MODEL> simulationModelGenerator,
      EnvironmentFactory<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> environmentFactory) {
    this(simulationModelGenerator, environmentFactory, RandomUtil.newGenerator());
  }

  public AbstractSimulation(
      SimulationModelGenerator<SIM_MODEL> simulationModelGenerator,
      EnvironmentFactory<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> environmentFactory,
      RandomGenerator random) {
    this.simulationModelGenerator = Preconditions.checkNotNull(simulationModelGenerator);
    this.environmentFactory = Preconditions.checkNotNull(environmentFactory);
    this.random = Preconditions.checkNotNull(random);
  }

  @Override
  public SIM_MODEL getState() {
    for (Environment<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> env : environments) {
      env.updateState(model);
    }
    return model;
  }

  @Override
  public void setState(SIM_MODEL model) {
    this.model = model;

    // Close the existing environment simulations.
    environments.forEach(Environment::close);

    environments.clear();
    for (ENV_MODEL environmentModel : model.getEnvironments()) {
      environments.add(environmentFactory.create(environmentModel));
    }

    environments.forEach(env -> env.setState(model));
  }

  public List<Environment<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE>> getEnvironments() {
    return environments;
  }

  @Override
  public void addObserver(SimulationObserver<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> observer) {
    environments.forEach(env -> env.addObserver(observer));
  }

  @Override
  public void removeObserver(final SimulationObserver<SIM_MODEL, ENV_MODEL, ENT_MODEL, ENT_TYPE> observer) {
    environments.forEach(env -> env.removeObserver(observer));
  }

  @Override
  public SIM_MODEL generateRandomState() {
    return simulationModelGenerator.generate(random);
  }

  @Override
  public void step(int stepCount) {
    if (logger.isDebugEnabled()) {
      logger.debug("Performing iteration {}", model.getIteration());
    }

    if (model.getSeed() == null) {
      model.setSeed(String.valueOf(new SecureRandom().nextLong()));
    }

    random.setSeed(Long.valueOf(model.getSeed()));

    // Create separate random generators for each environment so that the simulation remains deterministic.
    Map<String, RandomGenerator> environmentRandomGenerators = new HashMap<>();
    environments.forEach(env -> environmentRandomGenerators
            .put(env.getModel().getId(), new Well19937c(random.nextLong())));

    environments.parallelStream().forEach(env -> {
      for(int cnt = 1; cnt <= stepCount; cnt++) {
        env.update(model, environmentRandomGenerators.get(env.getModel().getId()));
      }
    });

    if (logger.isDebugEnabled()) {
      // Print seed after updating environments in case seed is changed.
      logger.debug("seed = {}", model.getSeed());
    }

    // Set the seed to be used for the next step.
    model.setSeed(String.valueOf(random.nextLong()));
    model.setIteration(model.getIteration()+stepCount);
  }

  @Override
  public void close() {
    environments.forEach(Environment::close);
  }
}
