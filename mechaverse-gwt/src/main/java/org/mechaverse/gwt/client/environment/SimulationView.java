package org.mechaverse.gwt.client.environment;

import org.mechaverse.simulation.ant.api.model.Environment;

import com.google.gwt.user.client.ui.Composite;

/**
 * A view which displays a simulation.
 *
 * @author thorntonv@mechaverse.org
 */
public class SimulationView extends Composite {

  private final EnvironmentView environmentView = new EnvironmentView();

  public SimulationView() {
    initWidget(environmentView);
  }

  public void setEnvironment(Environment environment) {
    environmentView.setEnvironment(environment);
  }
}
