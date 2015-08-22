package org.mechaverse.gwt.common.client.webconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A bar that is used to display notification messages to the user.
 */
public class NotificationBar extends Composite {

  interface MyUiBinder extends UiBinder<Widget, NotificationBar> {}


  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField Label label;
  private final WebConsoleResourceBundle.Style style;

  public NotificationBar() {
    this(WebConsoleResourceBundle.INSTANCE.css());
  }

  public NotificationBar(WebConsoleResourceBundle.Style style) {
    this.style = style;
    initWidget(uiBinder.createAndBindUi(this));
  }

  public void show(String text) {
    label.setText(text);
    label.setStyleName(style.notificationNoticeText());
    RootPanel.get().add(this);
  }

  public void showLoading() {
    show("Loading ...");
  }

  public void showSaving() {
    show("Saving ...");
  }

  public void showError(String text) {
    label.setText(text);
    label.setStyleName(style.notificationErrorText());
    RootPanel.get().add(this);
  }

  public void hide() {
    removeFromParent();
  }
}
