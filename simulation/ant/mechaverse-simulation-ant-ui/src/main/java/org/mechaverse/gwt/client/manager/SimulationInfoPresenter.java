package org.mechaverse.gwt.client.manager;

import org.mechaverse.gwt.client.environment.SimulationPresenter.SimulationPlace;
import org.mechaverse.gwt.common.shared.ManagerGwtRpcServiceAsync;
import org.mechaverse.service.manager.api.model.InstanceInfo;
import org.mechaverse.service.manager.api.model.SimulationConfig;
import org.mechaverse.service.manager.api.model.SimulationInfo;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Presenter for simulation information.
 */
public class SimulationInfoPresenter extends AbstractActivity
    implements SimulationInfoView.Observer {

  public static class SimulationInfoPlace extends Place {

    private String simulationId;

    public SimulationInfoPlace(String simulationId) {
      this.simulationId = simulationId;
    }

    public String getSimulationId() {
      return simulationId;
    }

    public static class Tokenizer implements PlaceTokenizer<SimulationInfoPlace> {
      @Override
      public String getToken(SimulationInfoPlace place) {
        return place.getSimulationId();
      }

      @Override
      public SimulationInfoPlace getPlace(String token) {
        return new SimulationInfoPlace(token);
      }
    }
  }

  private String simulationId;
  private final SimulationInfoView view;
  private final ManagerGwtRpcServiceAsync service = ManagerGwtRpcServiceAsync.Util.getInstance();
  private final PlaceController placeController;

  public SimulationInfoPresenter(SimulationInfoPlace place, ManagerClientFactory clientFactory) {
    this.view = clientFactory.getSimulationInfoView();
    view.setObserver(this);
    setSimulationId(place.getSimulationId());
    this.placeController = clientFactory.getPlaceController();
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view);
  }

  public void setSimulationId(String simulationId) {
    this.simulationId = simulationId;
    service.getSimulationInfo(simulationId, new AsyncCallback<SimulationInfo>() {
      @Override
      public void onFailure(Throwable arg0) {}

      @Override
      public void onSuccess(SimulationInfo simulationInfo) {
        view.setSimulationInfo(simulationInfo);
      }
    });
  }

  public SimulationInfoView getView() {
    return view;
  }

  @Override
  public void updateConfig(final SimulationConfig config) {
    service.updateSimulationConfig(config, new AsyncCallback<Void>() {
      @Override
      public void onSuccess(Void arg0) {
        setSimulationId(simulationId);
      }

      @Override
      public void onFailure(Throwable ex) {
        Window.alert(ex.getMessage());
      }
    });
  }

  @Override
  public void onSelectInstance(InstanceInfo instance) {
    placeController.goTo(new SimulationPlace(
      simulationId, instance.getInstanceId(), instance.getIteration()));
  }
}
