package org.mechaverse.gwt.client.environment;

import org.mechaverse.gwt.shared.MechaverseGwtRpcServiceAsync;
import org.mechaverse.simulation.ant.api.model.SimulationModel;
import org.mechaverse.simulation.api.SimulationStateKey;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ScrollEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class SimulationPresenter extends AbstractActivity {

  public static class SimulationPlace extends Place {

    private String simulationId;
    private String instanceId;
    private long iteration;

    public SimulationPlace(String simulationId, String instanceId, long iteration) {
      this.simulationId = simulationId;
      this.instanceId = instanceId;
      this.iteration = iteration;
    }

    public String getSimulationId() {
      return simulationId;
    }

    public String getInstanceId() {
      return instanceId;
    }

    public long getIteration() {
      return iteration;
    }

    public static class Tokenizer implements PlaceTokenizer<SimulationPlace> {
      @Override
      public String getToken(SimulationPlace place) {
        return place.getSimulationId() + ":" + place.getInstanceId() + ":" + place.getIteration();
      }

      @Override
      public SimulationPlace getPlace(String token) {
        String[] elements = token.split(":");
        return new SimulationPlace(elements[0], elements[1], Long.parseLong(elements[2]));
      }
    }
  }

  private static final int UPDATE_INTERVAL = 1000;

  protected class UpdateTimer extends Timer {
    @Override
    public void run() {
      service.step(new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable ex) {
          Window.alert(ex.getMessage());
          cancel();
        }

        @Override
        public void onSuccess(Void arg0) {
          service.getModel(new AsyncCallback<SimulationModel>() {
            @Override
            public void onFailure(Throwable ex) {}

            @Override
            public void onSuccess(SimulationModel state) {
              setState(state);
            }
          });
        }
      });
    }
  };

  private final MechaverseGwtRpcServiceAsync service =
      MechaverseGwtRpcServiceAsync.Util.getInstance();

  private UpdateTimer updateTimer = new UpdateTimer();
  private SimulationView view;

  public SimulationPresenter(SimulationStateKey simulationStateKey, SimulationView view) {
    this.view = view;

    service.loadState(simulationStateKey.getSimulationId(), simulationStateKey.getInstanceId(),
        simulationStateKey.getIteration(), new AsyncCallback<SimulationModel>() {
          @Override
          public void onFailure(Throwable ex) {}

          @Override
          public void onSuccess(SimulationModel model) {
            setState(model);
            addScrollHandler();
            updateTimer.scheduleRepeating(UPDATE_INTERVAL);
          }
        });
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view);
  }

  public void setState(SimulationModel state) {
    view.setEnvironment(state.getEnvironment());
  }

  public SimulationView getView() {
    return view;
  }

  private void addScrollHandler() {
    Window.addWindowScrollHandler(new Window.ScrollHandler() {
      @Override
      public void onWindowScroll(ScrollEvent arg0) {
        updateTimer.cancel();
        updateTimer.scheduleRepeating(UPDATE_INTERVAL);
      }
    });
  }
}
