package org.mechaverse.gwt.client.environment;

import org.mechaverse.api.model.simulation.ant.SimulationState;
import org.mechaverse.api.service.MechaverseService.SimulationStatus;
import org.mechaverse.gwt.shared.MechaverseGwtRpcServiceAsync;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SimulationPresenter {

  public static final String INITIAL_STATE_KEY = "0000000000";

  protected class UpdateTimer extends Timer {
    @Override
    public void run() {
      service.setStatus(SimulationStatus.STEPPING, new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable ex) {
          Window.alert(ex.getMessage());
        }

        @Override
        public void onSuccess(Void arg0) {
          service.getCurrentState(new AsyncCallback<SimulationState>() {
            @Override
            public void onFailure(Throwable ex) {
              Window.alert(ex.getMessage());
            }

            @Override
            public void onSuccess(SimulationState state) {
              setState(state);
            }
          });
        }
      });
    }
  };

  private final MechaverseGwtRpcServiceAsync service = MechaverseGwtRpcServiceAsync.Util
      .getInstance();
  
  private UpdateTimer updateTimer = new UpdateTimer();
  private SimulationView view;

  public SimulationPresenter(SimulationView view) {
    this(INITIAL_STATE_KEY, view);
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
            updateTimer.scheduleRepeating(1000);
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
