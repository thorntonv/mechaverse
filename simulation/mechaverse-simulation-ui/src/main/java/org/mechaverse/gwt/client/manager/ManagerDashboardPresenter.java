package org.mechaverse.gwt.client.manager;

import java.util.List;

import org.mechaverse.gwt.client.manager.SimulationInfoPresenter.SimulationInfoPlace;
import org.mechaverse.gwt.common.client.webconsole.NotificationBar;
import org.mechaverse.gwt.common.shared.ManagerGwtRpcServiceAsync;
import org.mechaverse.manager.api.model.SimulationInfo;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/*
 * Presenter for the manager dashboard.
 */
public class ManagerDashboardPresenter extends AbstractActivity
    implements ManagerDashboardView.Observer {

  public static class ManagerDashboardPlace extends Place {

    public static final String NAME = "Dashboard";

    public static class Tokenizer implements PlaceTokenizer<ManagerDashboardPlace> {
      @Override
      public ManagerDashboardPlace getPlace(String token) {
        return new ManagerDashboardPlace();
      }

      @Override
      public String getToken(ManagerDashboardPlace place) {
        return "dashboard";
      }
    }
  }

  private final ManagerGwtRpcServiceAsync service = ManagerGwtRpcServiceAsync.Util.getInstance();

  private final ManagerDashboardView view;
  private final PlaceController placeController;
  private final NotificationBar notificationBar;

  public ManagerDashboardPresenter(ManagerClientFactory clientFactory) {
    this.notificationBar = clientFactory.getNotificationBar();
    this.placeController = clientFactory.getPlaceController();
    this.view = clientFactory.getDashboardView();

    view.setObserver(this);
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view);
    loadData();
  }

  public void loadData() {
    notificationBar.showLoading();
    service.getSimulationInfo(new AsyncCallback<List<SimulationInfo>>() {
      @Override
      public void onFailure(Throwable ex) {
        notificationBar.showError(ex.getMessage());
      }

      @Override
      public void onSuccess(List<SimulationInfo> simulationInfoList) {
        notificationBar.hide();
        view.setSimulationInfo(simulationInfoList);
      }
    });
  }

  @Override
  public void onCreateSimulation() {
    notificationBar.show("Creating new simulation");
    service.createSimulation("New simulation", new AsyncCallback<SimulationInfo>() {
      @Override
      public void onFailure(Throwable ex) {
        notificationBar.showError(ex.getMessage());
      }

      @Override
      public void onSuccess(SimulationInfo simulationInfo) {
        notificationBar.hide();
        loadData();
      }
    });
  }

  @Override
  public void onDeleteSimulation(SimulationInfo simulationInfo) {
    if (Window.confirm("Are you certain you would like to delete "
        + simulationInfo.getName() + "?")) {
      notificationBar.show("Deleting simulation");
      service.deleteSimulation(simulationInfo.getSimulationId(), new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable ex) {
          notificationBar.showError(ex.getMessage());
        }

        @Override
        public void onSuccess(Void result) {
          notificationBar.hide();
          loadData();
        }
      });
    }
  }

  @Override
  public void onSelectSimulation(SimulationInfo selectedSimulationInfo) {
    placeController.goTo(new SimulationInfoPlace(selectedSimulationInfo.getSimulationId()));
  }

  @Override
  public void onRefresh() {
    loadData();
  }

  public ManagerDashboardView getView() {
    return view;
  }
}
