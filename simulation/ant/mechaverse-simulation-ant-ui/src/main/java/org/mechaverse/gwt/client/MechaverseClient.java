package org.mechaverse.gwt.client;

import org.mechaverse.gwt.client.manager.DefaultManagerClientFactoryImpl;
import org.mechaverse.gwt.client.manager.ManagerActivityMapper;
import org.mechaverse.gwt.client.manager.ManagerClientFactory;
import org.mechaverse.gwt.client.manager.ManagerDashboardPresenter.ManagerDashboardPlace;
import org.mechaverse.gwt.client.manager.ManagerPlaceHistoryMapper;
import org.mechaverse.gwt.common.client.webconsole.WebConsoleResourceBundle;
import org.mechaverse.gwt.common.client.webconsole.WebConsoleResourceBundle.TableResources;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.web.bindery.event.shared.EventBus;

/**
 * The main entry point.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class MechaverseClient implements EntryPoint {

  private Place defaultPlace = new ManagerDashboardPlace();

  @Override
  public void onModuleLoad() {
    ensureCssInjected();

    ManagerClientFactory clientFactory = new DefaultManagerClientFactoryImpl();

    EventBus eventBus = clientFactory.getEventBus();
    PlaceController placeController = clientFactory.getPlaceController();

    // Start ActivityManager for the main widget with our ActivityMapper
    ActivityMapper activityMapper = new ManagerActivityMapper(clientFactory);
    ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
    activityManager.setDisplay(clientFactory.getLayoutView());

    // Start PlaceHistoryHandler with our PlaceHistoryMapper
    ManagerPlaceHistoryMapper historyMapper= GWT.create(ManagerPlaceHistoryMapper.class);
    PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
    historyHandler.register(placeController, eventBus, defaultPlace);

    RootLayoutPanel.get().add(clientFactory.getLayoutView());

    historyHandler.handleCurrentHistory();
  }

  protected static void ensureCssInjected() {
    AntSimulationResourceBundle.INSTANCE.css().ensureInjected();
    WebConsoleResourceBundle.INSTANCE.css().ensureInjected();
    TableResources.INSTANCE.cellTableStyle().ensureInjected();
  }
}
