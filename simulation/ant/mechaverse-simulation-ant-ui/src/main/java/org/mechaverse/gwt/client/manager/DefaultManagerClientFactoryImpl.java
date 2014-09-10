package org.mechaverse.gwt.client.manager;

import org.mechaverse.gwt.client.environment.SimulationView;
import org.mechaverse.gwt.common.client.webconsole.NotificationBar;
import org.mechaverse.gwt.common.client.webconsole.WebConsoleLayoutView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.web.bindery.event.shared.EventBus;

public class DefaultManagerClientFactoryImpl implements ManagerClientFactory {

  private final EventBus eventBus = new SimpleEventBus();
  private final PlaceController placeController = new PlaceController(eventBus);
  private final WebConsoleLayoutView layoutView = new WebConsoleLayoutView();
  private final NotificationBar notificationBar = new NotificationBar();
  private final PlaceHistoryMapper placeHistoryMapper =
      (PlaceHistoryMapper) GWT.create(ManagerPlaceHistoryMapper.class);

  @Override
  public EventBus getEventBus() {
    return eventBus;
  }

  @Override
  public PlaceController getPlaceController() {
    return placeController;
  }

  @Override
  public ManagerDashboardView getDashboardView() {
    return new ManagerDashboardView(layoutView, placeHistoryMapper);
  }

  @Override
  public SimulationInfoView getSimulationInfoView() {
    return new SimulationInfoView(layoutView, placeHistoryMapper);
  }

  @Override
  public WebConsoleLayoutView getLayoutView() {
    return layoutView;
  }

  @Override
  public SimulationView getSimulationView() {
    return new SimulationView(layoutView, placeHistoryMapper);
  }

  @Override
  public NotificationBar getNotificationBar() {
    return notificationBar;
  }
}
