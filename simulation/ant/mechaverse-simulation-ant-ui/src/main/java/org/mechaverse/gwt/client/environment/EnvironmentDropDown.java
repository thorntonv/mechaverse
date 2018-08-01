package org.mechaverse.gwt.client.environment;

import org.mechaverse.gwt.common.client.util.renderer.StringRenderer;
import com.google.gwt.user.client.ui.ValueListBox;

/**
 * A dropdown for selecting an environment.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class EnvironmentDropDown extends ValueListBox<String> {

  public EnvironmentDropDown() {
    super(StringRenderer.INSTANCE);
  }
}
