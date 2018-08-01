package org.mechaverse.gwt.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * A bundle which contains common resources.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public interface SimulationResourceBundle extends ClientBundle {

  SimulationResourceBundle INSTANCE =
      GWT.create(SimulationResourceBundle.class);

  interface Style extends CssResource {

    String environmentPanel();
  }

  @Source("mechaverse.css")
  Style css();
}
