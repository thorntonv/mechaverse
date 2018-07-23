package org.mechaverse.simulation.common;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mechaverse.simulation.common.model.EnvironmentModel;
import org.mechaverse.simulation.common.model.SimulationModel;
import com.google.common.base.Preconditions;

@SuppressWarnings("unused")
public abstract class AbstractSimulationModelGenerator<
    SIM_MODEL extends SimulationModel<ENV_MODEL, ENT_MODEL, ENT_TYPE>,
    ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
    ENT_MODEL extends EntityModel<ENT_TYPE>,
    ENT_TYPE extends Enum<ENT_TYPE>>
    implements SimulationModelGenerator<SIM_MODEL> {

  private static final int DEFAULT_SUB_ENVIRONMENT_COUNT = 1;

  private final int subEnvironmentCount;
  private final EnvironmentModelGenerator<ENV_MODEL, ENT_MODEL, ENT_TYPE> environmentModelGenerator;

  protected AbstractSimulationModelGenerator(
      EnvironmentModelGenerator<ENV_MODEL, ENT_MODEL, ENT_TYPE> environmentModelGenerator) {
    this(environmentModelGenerator, DEFAULT_SUB_ENVIRONMENT_COUNT);
  }

  protected AbstractSimulationModelGenerator(
      EnvironmentModelGenerator<ENV_MODEL, ENT_MODEL, ENT_TYPE> environmentModelGenerator,
      int subEnvironmentCount) {
    this.environmentModelGenerator = Preconditions.checkNotNull(environmentModelGenerator);
    this.subEnvironmentCount = subEnvironmentCount;
  }

  @Override
  public SIM_MODEL generate(RandomGenerator random) {
    SIM_MODEL model = createModel();
    model.setIteration(0);
    model.setEnvironment(environmentModelGenerator.generate(random));
    for (int cnt = 1; cnt <= subEnvironmentCount; cnt++) {
      model.getSubEnvironments().add(environmentModelGenerator.generate(random));
    }
    model.setSeed(String.valueOf(random.nextLong()));
    return model;
  }

  protected abstract SIM_MODEL createModel();

}
