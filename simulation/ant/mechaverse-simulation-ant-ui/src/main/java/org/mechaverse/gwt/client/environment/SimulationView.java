package org.mechaverse.gwt.client.environment;

import org.mechaverse.simulation.ant.api.model.Environment;

import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * A view which displays a simulation.
 *
 * @author thorntonv@mechaverse.org
 */
public class SimulationView extends ScrollPanel {

  private final EnvironmentView environmentView = new EnvironmentView();

  public SimulationView() {
    add(environmentView);
  }

  public void setEnvironment(Environment environment) {
    environmentView.setEnvironment(environment);
  }
}
