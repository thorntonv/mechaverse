package org.mechaverse.gwt.client.environment;

import org.mechaverse.gwt.client.manager.ManagerClientFactory;
import org.mechaverse.gwt.common.client.webconsole.NotificationBar;
import org.mechaverse.gwt.shared.MechaverseGwtRpcServiceAsync;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.user.client.Timer;
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

    private boolean inProgress = false;

    @Override
    public void run() {
      if(!view.isAttached()) {
        return;
      }

      if (inProgress) {
        return;
      }

      inProgress = true;
      schedule(UPDATE_INTERVAL);

      service.step(new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable ex) {
          notificationBar.showError("Step failed: " + ex.getClass().getName() + ": "
              + ex.getMessage());
          finishUpdate();
        }

        @Override
        public void onSuccess(Void result) {
          service.getStateImage(new AsyncCallback<String>() {
            @Override
            public void onFailure(final Throwable ex) {
              notificationBar.showError("Get state image: " + ex.getClass().getName() + ": "
                      + ex.getMessage());
              finishUpdate();
            }

            @Override
            public void onSuccess(final String result) {
              view.setStateImage(result);
              finishUpdate();
            }
          });
        }
      });
    }

    @Override
    public void cancel() {
      super.cancel();
    }

    private void finishUpdate() {
      inProgress = false;
      schedule(0);
    }
  }

  private final MechaverseGwtRpcServiceAsync service =
      MechaverseGwtRpcServiceAsync.Util.getInstance();

  private final UpdateTimer updateTimer = new UpdateTimer();
  private final NotificationBar notificationBar;
  private SimulationView view;
  private HandlerRegistration scrollHandler;

  public SimulationPresenter(final String simulationId, final String instanceId,
      final long iteration, ManagerClientFactory clientFactory) {
    this.notificationBar = clientFactory.getNotificationBar();
    this.view = clientFactory.getSimulationView();

    view.addAttachHandler(new AttachEvent.Handler() {
      @Override
      public void onAttachOrDetach(AttachEvent event) {
        if (event.isAttached()) {
          loadState(simulationId, instanceId, iteration);
          addScrollHandler();
        } else {
          updateTimer.cancel();
          if (scrollHandler != null) {
            scrollHandler.removeHandler();
            scrollHandler = null;
          }
        }
      }
    });
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view);
  }

  public SimulationView getView() {
    return view;
  }

  private void loadState(String simulationId, String instanceId, long iteration) {
    notificationBar.showLoading();
    service.loadState(simulationId, instanceId, iteration, new AsyncCallback<Void>() {
      @Override
      public void onFailure(Throwable ex) {
        notificationBar.showError(ex.getMessage());
      }

      @Override
      public void onSuccess(Void retVal) {
        notificationBar.hide();
        updateTimer.schedule(UPDATE_INTERVAL);
      }
    });
  }

  private void addScrollHandler() {
    if (scrollHandler != null) {
      scrollHandler.removeHandler();
    }
    scrollHandler = view.addScrollHandler(new ScrollHandler() {
      @Override
      public void onScroll(ScrollEvent event) {
        updateTimer.cancel();
        updateTimer.schedule(UPDATE_INTERVAL);
      }
    });
  }
}
