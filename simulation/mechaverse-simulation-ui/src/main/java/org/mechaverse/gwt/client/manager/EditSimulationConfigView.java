package org.mechaverse.gwt.client.manager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.mechaverse.manager.api.model.SimulationConfig;

/**
 * A view that shows a form for editing a simulation configuration.
 */
public class EditSimulationConfigView extends Composite {

  interface MyUiBinder extends UiBinder<Widget, EditSimulationConfigView> {}

  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField TextBox minInstanceCount;
  @UiField TextBox maxInstanceCount;
  @UiField TextBox taskIterationCount;
  @UiField TextBox taskMaxDurationSeconds;
  @UiField ListBox simulationType;

  private SimulationConfig config;

  public EditSimulationConfigView() {
    initWidget(uiBinder.createAndBindUi(this));
    simulationType.addItem("ant");
    simulationType.addItem("primordial");
  }

  public void setSimulationConfig(SimulationConfig config) {
    this.config = config;
    minInstanceCount.setText(String.valueOf(config.getMinInstanceCount()));
    maxInstanceCount.setText(String.valueOf(config.getMaxInstanceCount()));
    taskIterationCount.setText(String.valueOf(config.getTaskIterationCount()));
    taskMaxDurationSeconds.setText(String.valueOf(config.getTaskMaxDurationInSeconds()));
    simulationType.setEnabled(true);
    if (config.getSimulationType() != null) {
      for (int idx = 0; idx < simulationType.getItemCount(); idx++) {
        if (config.getSimulationType().equalsIgnoreCase(simulationType.getItemText(idx))) {
          simulationType.setSelectedIndex(idx);
          simulationType.setEnabled(false);
        }
      }
    }
  }

  public SimulationConfig getSimulationConfig() {
    config.setMinInstanceCount(Integer.parseInt(minInstanceCount.getText()));
    config.setMaxInstanceCount(Integer.parseInt(maxInstanceCount.getText()));
    config.setTaskIterationCount(Integer.parseInt(taskIterationCount.getText()));
    config.setTaskMaxDurationInSeconds(Long.parseLong(taskMaxDurationSeconds.getText()));
    config.setSimulationType(simulationType.getItemText(simulationType.getSelectedIndex()));
    return config;
  }
}
