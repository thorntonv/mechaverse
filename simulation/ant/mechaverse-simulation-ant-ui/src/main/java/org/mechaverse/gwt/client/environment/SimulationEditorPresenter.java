package org.mechaverse.gwt.client.environment;

import java.util.Iterator;

import org.mechaverse.gwt.common.client.util.UUID;
import org.mechaverse.gwt.shared.MechaverseGwtRpcServiceAsync;
import org.mechaverse.simulation.ant.api.SimulationModelUtil;
import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.SimulationModel;
import org.mechaverse.simulation.api.SimulationStateKey;

import com.google.common.base.Preconditions;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A presenter for {@link SimulationEditorView}.
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

  private SimulationStateKey simulationStateKey;
  private String environmentId;
  private SimulationModel state;
  private final EnvironmentEditorPresenter environmentEditorPresenter;
  private final SimulationEditorView view;

  public SimulationEditorPresenter(
      SimulationStateKey simulationStateKey, SimulationEditorView view) {
    this.simulationStateKey = simulationStateKey;
    this.view = view;
    this.environmentEditorPresenter =
        new EnvironmentEditorPresenter(view.getEnvironmentEditorView());

    view.setObserver(new SimulationEditorViewObserver());
    loadState();
  }

  public void loadState() {
    view.setEnabled(false);
    service.loadState(simulationStateKey.getSimulationId(), simulationStateKey.getInstanceId(),
      simulationStateKey.getIteration(), new AsyncCallback<SimulationModel>() {
      @Override
      public void onFailure(Throwable caught) {
        saveInitialState();
      }

      @Override
      public void onSuccess(final SimulationModel state) {
        setState(state);
        view.setEnabled(true);
      }
    });
  }

  public void setState(SimulationModel state) {
    this.state = state;
    setEnvironment(environmentId);
    view.setAvailableEnvironments(SimulationModelUtil.getEnvironments(state));
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

    view.setEnabled(false);
    service.saveState(simulationStateKey.getSimulationId(), simulationStateKey.getInstanceId(),
      simulationStateKey.getIteration(), new AsyncCallback<Void>() {
      @Override
      public void onFailure(Throwable cause) {}

      @Override
      public void onSuccess(Void value) {
        if (callback != null) {
          callback.onSuccess(value);
        }
        view.setEnabled(true);
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
      env = SimulationModelUtil.getEnvironment(state, environmentId);
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
    view.setEnabled(false);
    service.getModel(new AsyncCallback<SimulationModel>() {
      @Override
      public void onFailure(Throwable arg0) {}

      @Override
      public void onSuccess(SimulationModel state) {
        service.setModel(state, new AsyncCallback<Void>() {
          @Override
          public void onFailure(Throwable cause) {}

          @Override
          public void onSuccess(Void arg0) {
            loadState();
            view.setEnabled(true);
          }
        });
      }
    });
  }
}
