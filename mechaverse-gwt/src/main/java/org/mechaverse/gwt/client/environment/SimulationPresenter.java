package org.mechaverse.gwt.client.environment;

import org.mechaverse.gwt.shared.MechaverseGwtRpcServiceAsync;
import org.mechaverse.simulation.ant.api.model.SimulationModel;
import org.mechaverse.simulation.api.SimulationStateKey;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ScrollEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SimulationPresenter {

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

    updateTimer.scheduleRepeating(UPDATE_INTERVAL);

    Window.addWindowScrollHandler(new Window.ScrollHandler() {
      @Override
      public void onWindowScroll(ScrollEvent arg0) {
        updateTimer.cancel();
        updateTimer.scheduleRepeating(UPDATE_INTERVAL);
      }
    });
  }

  public void setState(SimulationModel state) {
    view.setEnvironment(state.getEnvironment());
  }

  public SimulationView getView() {
    return view;
  }
}
