package org.mechaverse.gwt.client.manager;

import org.mechaverse.gwt.client.simulation.SimulationView;
import org.mechaverse.gwt.common.client.webconsole.NotificationBar;
import org.mechaverse.gwt.common.client.webconsole.WebConsoleLayoutView;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

public interface ManagerClientFactory {

  EventBus getEventBus();
  PlaceController getPlaceController();
  ManagerDashboardView getDashboardView();
  SimulationInfoView getSimulationInfoView();
  SimulationView getSimulationView();
  WebConsoleLayoutView getLayoutView();
  NotificationBar getNotificationBar();
}
