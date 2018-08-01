package org.mechaverse.gwt.client.simulation;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import org.mechaverse.gwt.client.SimulationResourceBundle;

/**
 * A view which displays an simulation.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class EnvironmentView extends SimplePanel {

  private Image image;

  public EnvironmentView() {
    addStyleName(SimulationResourceBundle.INSTANCE.css().environmentPanel());
    this.image = new Image();
    add(image);
  }

  public void setImage(String base64ImageData) {
    image.setUrl(base64ImageData);
  }
}
