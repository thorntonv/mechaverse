package org.mechaverse.gwt.common.client.webconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The web console layout UI.
 *
 * @author Vance Thornton
 */
public class WebConsoleLayoutView extends ResizeComposite implements AcceptsOneWidget {

  interface MyUiBinder extends UiBinder<Widget, WebConsoleLayoutView> {};

  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField SimplePanel topPanel;
  @UiField SimpleLayoutPanel contentPanel;
  @UiField SimplePanel navPanel;
  @UiField FlowPanel actionPanel;

  public WebConsoleLayoutView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public void setTopWidget(IsWidget widget) {
    topPanel.setWidget(widget);
  }

  public void setNavWidget(IsWidget navWidget) {
    navPanel.setWidget(navWidget);
  }

  public void clearActionButtons() {
    actionPanel.clear();
  }

  public void addActionButton(IsWidget widget) {
    actionPanel.add(widget);
  }

  /**
   * Sets the content widget.
   */
  @Override
  public void setWidget(IsWidget widget) {
    contentPanel.setWidget(widget);
  }
}
