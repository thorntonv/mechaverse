package org.mechaverse.gwt.client.environment;

import java.util.Iterator;

import org.mechaverse.gwt.client.util.UUID;
import org.mechaverse.gwt.shared.MechaverseGwtRpcServiceAsync;
import org.mechaverse.simulation.ant.api.SimulationStateUtil;
import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.SimulationState;

import com.google.common.base.Preconditions;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A presenter for {@link SimulationEditorView}.
 *
 * @author thorntonv@mechaverse.org
 */
public class SimulationEditorPresenter {

  protected class SimulationEditorViewObserver implements SimulationEditorView.Observer {

    @Override
    public void onNew() {
      createNewEnvironment();
    }

    @Override
    public void onSave() {
      save();
    }

    @Override
    public void onDelete() {
      deleteEnvironment();
    }

    @Override
    public void onEnvironmentSelected(String environmentId) {
      setEnvironment(environmentId);
    }
  }

  private final MechaverseGwtRpcServiceAsync service =
      MechaverseGwtRpcServiceAsync.Util.getInstance();

  private String key;
  private String environmentId;
  private SimulationState state;
  private final EnvironmentEditorPresenter environmentEditorPresenter;
  private final SimulationEditorView view;

  public SimulationEditorPresenter(SimulationEditorView view) {
    this(SimulationPresenter.INITIAL_STATE_KEY, view);
  }

  public SimulationEditorPresenter(String key, SimulationEditorView view) {
    this.key = key;
    this.view = view;
    this.environmentEditorPresenter =
        new EnvironmentEditorPresenter(view.getEnvironmentEditorView());

    view.setObserver(new SimulationEditorViewObserver());
    loadState();
  }

  public void loadState() {
    service.loadState(key, new AsyncCallback<SimulationState>() {
      @Override
      public void onFailure(Throwable caught) {
        saveInitialState();
      }

      @Override
      public void onSuccess(final SimulationState state) {
        setState(state);
      }
    });
  }

  public void setState(SimulationState state) {
    this.state = state;
    setEnvironment(environmentId);
    view.setAvailableEnvironments(SimulationStateUtil.getEnvironments(state));
  }

  public void createNewEnvironment() {
    Preconditions.checkNotNull(state);

    final Environment newEnvironment = new Environment();
    newEnvironment.setId(UUID.uuid().toString().toLowerCase());
    newEnvironment.setWidth(25);
    newEnvironment.setHeight(25);
    state.getSubEnvironments().add(newEnvironment);

    save(new AsyncCallback<Void>() {
      @Override
      public void onFailure(Throwable error) {}

      @Override
      public void onSuccess(Void value) {
        environmentId = newEnvironment.getId();
        loadState();
      }
    });
  }

  public void save() {
    save(null);
  }

  public void save(final AsyncCallback<Void> callback) {
    Preconditions.checkNotNull(state);

    service.saveState(key, state, new AsyncCallback<Void>() {
      @Override
      public void onFailure(Throwable cause) {
        Window.alert(cause.getMessage());
      }

      @Override
      public void onSuccess(Void value) {
        if (callback != null) {
          callback.onSuccess(value);
        }
      }
    });
  }

  public void deleteEnvironment() {
    Preconditions.checkNotNull(state);

    Iterator<Environment> environmentIt = state.getSubEnvironments().iterator();
    while (environmentIt.hasNext()) {
      Environment env = environmentIt.next();
      if (env.getId().equalsIgnoreCase(environmentId)) {
        environmentIt.remove();
        save(new AsyncCallback<Void>() {
          @Override
          public void onFailure(Throwable arg0) {}

          @Override
          public void onSuccess(Void arg0) {
            environmentId = null;
            loadState();
          }
        });
      }
    }
  }

  public void setEnvironment(final String environmentId) {
    Preconditions.checkNotNull(state);

    this.environmentId = environmentId;
    Environment env = state.getEnvironment();
    if (environmentId != null) {
      env = SimulationStateUtil.getEnvironment(state, environmentId);
      if (env == null) {
        env = state.getEnvironment();
      }
    }
    view.setDeleteEnabled(!env.getId().equals(state.getEnvironment().getId()));
    environmentEditorPresenter.setEnvironment(env);
  }

  public SimulationEditorView getView() {
    return view;
  }

  protected void saveInitialState() {
    service.getCurrentState(new AsyncCallback<SimulationState>() {
      @Override
      public void onFailure(Throwable arg0) {}

      @Override
      public void onSuccess(SimulationState state) {
        service.saveState(key, state, new AsyncCallback<Void>() {
          @Override
          public void onFailure(Throwable cause) {
            Window.alert(cause.getMessage());
          }

          @Override
          public void onSuccess(Void arg0) {
            Window.alert("Created initial state.");
            loadState();
          }
        });
      }
    });
  }
}
