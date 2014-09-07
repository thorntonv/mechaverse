package org.mechaverse.gwt.common.client.webconsole;

import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;

/**
 * A button for performing a refresh.
 *
 * @author Vance Thornton
 */
public class RefreshButton extends Button {

  public RefreshButton() {
    Image img = new Image(WebConsoleResourceBundle.INSTANCE.refresh().getSafeUri());
    img.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
    img.getElement().getStyle().setOpacity(.667);
    getElement().appendChild(img.getElement());
  }
}
