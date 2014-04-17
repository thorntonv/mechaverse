package org.mechaverse.gwt.client.util.renderer;

import org.mechaverse.api.model.simulation.ant.Environment;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * A renderer which renders an environment ID.
 * 
 * @author thorntonv@mechaverse.org
 */
public class EnvironmentRenderer extends AbstractRenderer<Environment> {

  public static final EnvironmentRenderer INSTANCE = new EnvironmentRenderer();

  @Override
  public String render(Environment env) {
    return env.getId();
  }
}
