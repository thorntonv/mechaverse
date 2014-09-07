package org.mechaverse.gwt.client.manager;

import org.mechaverse.gwt.client.environment.SimulationPresenter;
import org.mechaverse.gwt.client.environment.SimulationPresenter.SimulationPlace;
import org.mechaverse.gwt.client.manager.ManagerDashboardPresenter.ManagerDashboardPlace;
import org.mechaverse.gwt.client.manager.SimulationInfoPresenter.SimulationInfoPlace;
import org.mechaverse.simulation.api.SimulationStateKey;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class ManagerActivityMapper implements ActivityMapper {
  private ManagerClientFactory clientFactory;

  public ManagerActivityMapper(ManagerClientFactory clientFactory) {
    super();
    this.clientFactory = clientFactory;
  }

  @Override
  public Activity getActivity(Place place) {
    clientFactory.getLayoutView().clearActionButtons();

    if (place instanceof ManagerDashboardPlace) {
      return new ManagerDashboardPresenter(clientFactory);
    } else if (place instanceof SimulationInfoPlace) {
      return new SimulationInfoPresenter((SimulationInfoPlace) place, clientFactory);
    } else if (place instanceof SimulationPlace) {
      SimulationPlace simulationPlace = (SimulationPlace) place;
      return new SimulationPresenter(new SimulationStateKey(simulationPlace.getSimulationId(),
          simulationPlace.getInstanceId(), simulationPlace.getIteration()),
          clientFactory.getSimulationView());
    }
    return null;
  }
}
