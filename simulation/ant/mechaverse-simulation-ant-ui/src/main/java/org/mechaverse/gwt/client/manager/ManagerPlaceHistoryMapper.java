package org.mechaverse.gwt.client.manager;

import org.mechaverse.gwt.client.environment.SimulationPresenter.SimulationPlace;
import org.mechaverse.gwt.client.manager.ManagerDashboardPresenter.ManagerDashboardPlace;
import org.mechaverse.gwt.client.manager.SimulationInfoPresenter.SimulationInfoPlace;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;

@WithTokenizers({
  ManagerDashboardPlace.Tokenizer.class,
  SimulationPlace.Tokenizer.class,
  SimulationInfoPlace.Tokenizer.class
})
public interface ManagerPlaceHistoryMapper extends PlaceHistoryMapper {}
