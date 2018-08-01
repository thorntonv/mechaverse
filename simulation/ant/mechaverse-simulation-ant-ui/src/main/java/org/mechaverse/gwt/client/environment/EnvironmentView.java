package org.mechaverse.gwt.client.environment;

import org.mechaverse.gwt.client.AntSimulationResourceBundle;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * A view which displays an environment.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class EnvironmentView extends SimplePanel {

  private Image image;

  public EnvironmentView() {
    addStyleName(AntSimulationResourceBundle.INSTANCE.css().environmentPanel());
    this.image = new Image();

    add(image);
  }

  public void setImage(String imageData) {
    image.setUrl(imageData);
  }
}
