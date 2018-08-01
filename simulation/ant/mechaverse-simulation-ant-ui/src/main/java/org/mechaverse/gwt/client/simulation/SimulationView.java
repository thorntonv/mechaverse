package org.mechaverse.gwt.client.simulation;

import org.mechaverse.gwt.client.manager.ManagerDashboardPresenter.ManagerDashboardPlace;
import org.mechaverse.gwt.common.client.webconsole.BasicNavMenu;
import org.mechaverse.gwt.common.client.webconsole.WebConsoleLayoutView;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * A view that displays a simulation.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class SimulationView extends ScrollPanel {

  private final EnvironmentView environmentView = new EnvironmentView();

  public SimulationView(WebConsoleLayoutView layoutView, PlaceHistoryMapper placeHistoryMapper) {
    add(environmentView);

    layoutView.setNavWidget(BasicNavMenu.newBuilder(placeHistoryMapper)
      .addLink(ManagerDashboardPlace.NAME, new ManagerDashboardPlace())
      .build());
  }

  public void setStateImage(String imageData) {
    environmentView.setImage(imageData);
  }
}
