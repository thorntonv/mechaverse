package org.mechaverse.gwt.client.util.renderer;

import org.mechaverse.simulation.common.model.EnvironmentModel;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * A renderer which renders an environment ID.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class EnvironmentRenderer extends AbstractRenderer<EnvironmentModel> {

  public static final EnvironmentRenderer INSTANCE = new EnvironmentRenderer();

  @Override
  public String render(EnvironmentModel env) {
    return env.getId();
  }
}
