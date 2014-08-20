package org.mechaverse.gwt.client;

import org.mechaverse.gwt.client.environment.SimulationEditorPresenter;
import org.mechaverse.gwt.client.environment.SimulationEditorView;
import org.mechaverse.gwt.client.environment.SimulationPresenter;
import org.mechaverse.gwt.client.environment.SimulationView;
import org.mechaverse.simulation.api.SimulationStateKey;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * The main entry point.
 *
 * @author thorntonv@mechaverse.org
 */
public class MechaverseClient implements EntryPoint {

  @Override
  public void onModuleLoad() {
    ensureCssInjected();

    SimulationStateKey simulationStateKey = new SimulationStateKey("", "", 0);
    String editParam = Window.Location.getParameter("edit");
    if (editParam != null && editParam.equalsIgnoreCase("true")) {
      SimulationEditorPresenter presenter =
          new SimulationEditorPresenter(simulationStateKey, new SimulationEditorView());
      RootLayoutPanel.get().add(presenter.getView());
    } else {
      SimulationPresenter presenter = new SimulationPresenter(simulationStateKey, new SimulationView());
      RootPanel.get().add(presenter.getView());
    }
  }

  protected static void ensureCssInjected() {
    MechaverseResourceBundle.INSTANCE.css().ensureInjected();
  }
}
