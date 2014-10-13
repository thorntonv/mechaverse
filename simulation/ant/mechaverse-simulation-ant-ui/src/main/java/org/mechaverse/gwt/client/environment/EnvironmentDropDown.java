package org.mechaverse.gwt.client.environment;

import org.mechaverse.gwt.client.util.renderer.EnvironmentRenderer;
import org.mechaverse.simulation.ant.api.model.Environment;

import com.google.gwt.user.client.ui.ValueListBox;

/**
 * A dropdown for selecting an environment.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class EnvironmentDropDown extends ValueListBox<Environment> {

  public EnvironmentDropDown() {
    super(EnvironmentRenderer.INSTANCE);
  }
}
