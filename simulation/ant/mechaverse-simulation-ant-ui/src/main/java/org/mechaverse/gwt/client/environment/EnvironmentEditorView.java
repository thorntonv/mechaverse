package org.mechaverse.gwt.client.environment;

import org.mechaverse.simulation.common.model.EnvironmentModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;

/**
 * A view for editing an environment.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class EnvironmentEditorView extends ResizeComposite {

  interface MyUiBinder extends UiBinder<Widget, EnvironmentEditorView> {}


  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField EntityToolbar toolbar;
  @UiField EnvironmentView environmentView;

  public EnvironmentEditorView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public void setEnvironment(EnvironmentModel environment) {
    environmentView.setEnvironment(environment);
  }

  public EntityToolbar getToolbar() {
    return toolbar;
  }

  public EnvironmentView getEnvironmentView() {
    return environmentView;
  }
}
