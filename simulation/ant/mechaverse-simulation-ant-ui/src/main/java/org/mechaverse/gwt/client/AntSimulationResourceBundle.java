package org.mechaverse.gwt.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * A bundle which contains common resources.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public interface AntSimulationResourceBundle extends ClientBundle {

  AntSimulationResourceBundle INSTANCE =
      GWT.create(AntSimulationResourceBundle.class);

  interface Style extends CssResource {

    String entityToolbar();
    String entityButton();
    String entityButtonSelected();

    String environmentPanel();
  }

  @Source("mechaverse.css")
  Style css();
}
