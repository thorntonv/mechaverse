package org.mechaverse.gwt.client.manager;

import org.mechaverse.service.manager.api.model.SimulationConfig;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * A view that shows a form for editing a simulation configuration.
 */
public class EditSimulationConfigView extends Composite {

  interface MyUiBinder extends UiBinder<Widget, EditSimulationConfigView> {};

  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField TextBox minInstanceCount;
  @UiField TextBox maxInstanceCount;
  @UiField TextBox taskIterationCount;
  @UiField TextBox taskMaxDurationSeconds;

  private SimulationConfig config;

  public EditSimulationConfigView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public void setSimulationConfig(SimulationConfig config) {
    this.config = config;
    minInstanceCount.setText(String.valueOf(config.getMinInstanceCount()));
    maxInstanceCount.setText(String.valueOf(config.getMaxInstanceCount()));
    taskIterationCount.setText(String.valueOf(config.getTaskIterationCount()));
    taskMaxDurationSeconds.setText(String.valueOf(config.getTaskMaxDurationInSeconds()));
  }

  public SimulationConfig getSimulationConfig() {
    config.setMinInstanceCount(Integer.parseInt(minInstanceCount.getText()));
    config.setMaxInstanceCount(Integer.parseInt(maxInstanceCount.getText()));
    config.setTaskIterationCount(Integer.parseInt(taskIterationCount.getText()));
    config.setTaskMaxDurationInSeconds(Integer.parseInt(taskMaxDurationSeconds.getText()));
    return config;
  }
}
