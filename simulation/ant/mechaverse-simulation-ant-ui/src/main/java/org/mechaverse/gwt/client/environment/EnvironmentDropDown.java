package org.mechaverse.gwt.client.environment;

import org.mechaverse.gwt.client.util.renderer.EnvironmentRenderer;
import org.mechaverse.simulation.common.model.EnvironmentModel;

import com.google.gwt.user.client.ui.ValueListBox;

/**
 * A dropdown for selecting an environment.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class EnvironmentDropDown extends ValueListBox<EnvironmentModel> {

  public EnvironmentDropDown() {
    super(EnvironmentRenderer.INSTANCE);
  }
}
