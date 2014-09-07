package org.mechaverse.gwt.common.client.webconsole;

import com.google.gwt.user.client.ui.Button;

/**
 * A button for performing an action.
 *
 * @author Vance Thornton
 */
public class ActionButton extends Button {

  public ActionButton() {
    setStyleName(WebConsoleResourceBundle.INSTANCE.css().actionButton());
  }

  public ActionButton(String text) {
    super(text);
    setStyleName(WebConsoleResourceBundle.INSTANCE.css().actionButton());
  }
}
