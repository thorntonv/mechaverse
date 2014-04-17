package org.mechaverse.gwt.client.environment;

import org.mechaverse.api.model.simulation.ant.Environment;
import org.mechaverse.gwt.client.util.renderer.EnvironmentRenderer;

import com.google.gwt.user.client.ui.ValueListBox;

/**
 * A dropdown for selecting an environment.
 * 
 * @author thorntonv@mechaverse.org
 */
public class EnvironmentDropDown extends ValueListBox<Environment> {
  
  public EnvironmentDropDown() {
    super(EnvironmentRenderer.INSTANCE);
  }
}
