package org.mechaverse.gwt.client.environment;

import org.mechaverse.gwt.shared.MechaverseGwtRpcServiceAsync;
import org.mechaverse.simulation.ant.api.model.SimulationState;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ScrollEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SimulationPresenter {

  public static final String INITIAL_STATE_KEY = "0000000000";

  private static final int UPDATE_INTERVAL = 1000;

  protected class UpdateTimer extends Timer {
    @Override
    public void run() {
      service.step(new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable ex) {
          cancel();
        }

        @Override
        public void onSuccess(Void arg0) {
          service.getCurrentState(new AsyncCallback<SimulationState>() {
            @Override
            public void onFailure(Throwable ex) {}

            @Override
            public void onSuccess(SimulationState state) {
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

  public SimulationPresenter(SimulationView view) {
    this(INITIAL_STATE_KEY, view);

    Window.addWindowScrollHandler(new Window.ScrollHandler() {
      @Override
      public void onWindowScroll(ScrollEvent arg0) {
        updateTimer.cancel();
        updateTimer.scheduleRepeating(UPDATE_INTERVAL);
      }
    });
  }

  public SimulationPresenter(String key, SimulationView view) {
    this.view = view;

    service.loadState(key, new AsyncCallback<SimulationState>() {

      @Override
      public void onFailure(Throwable cause) {
        Window.alert(cause.getMessage());
      }

      @Override
      public void onSuccess(SimulationState state) {
        service.setCurrentState(state, new AsyncCallback<Void>() {
          @Override
          public void onFailure(Throwable cause) {
            Window.alert(cause.getMessage());
          }

          @Override
          public void onSuccess(Void result) {
            updateTimer.scheduleRepeating(UPDATE_INTERVAL);
          }
        });
      }
    });
  }

  public void setState(SimulationState state) {
    view.setEnvironment(state.getEnvironment());
  }

  public SimulationView getView() {
    return view;
  }
}
