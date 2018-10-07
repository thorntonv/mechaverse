package org.mechaverse.gwt.client.manager;

import java.util.ArrayList;

import org.mechaverse.gwt.client.manager.ManagerDashboardPresenter.ManagerDashboardPlace;
import org.mechaverse.gwt.common.client.webconsole.ActionButton;
import org.mechaverse.gwt.common.client.webconsole.BasicNavMenu;
import org.mechaverse.gwt.common.client.webconsole.RefreshButton;
import org.mechaverse.gwt.common.client.webconsole.WebConsoleLayoutView;
import org.mechaverse.gwt.common.client.webconsole.WebConsoleResourceBundle.TableResources;
import org.mechaverse.manager.api.model.InstanceInfo;
import org.mechaverse.manager.api.model.SimulationConfig;
import org.mechaverse.manager.api.model.SimulationInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ListDataProvider;

/**
 * A view that displays information about a simulation. A form for editing the simulation
 * configuration and a list of instances are shown.
 */
public class SimulationInfoView extends Composite {

  public interface Observer {

    void updateConfig(SimulationConfig config);
    void onSelectInstance(InstanceInfo instance);
    void onRefresh();
  }


  interface MyUiBinder extends UiBinder<Widget, SimulationInfoView> {}


  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);


  private static final int DEFAULT_PAGE_SIZE = 20;

  private final TextColumn<InstanceInfo> idColumn = new TextColumn<InstanceInfo>() {
    @Override
    public String getValue(InstanceInfo instanceInfo) {
      return instanceInfo.getInstanceId();
    }
  };

  private final TextColumn<InstanceInfo> iterationColumn = new TextColumn<InstanceInfo>() {
    @Override
    public String getValue(InstanceInfo instanceInfo) {
      return String.valueOf(instanceInfo.getIteration());
    }
  };

  private final TextColumn<InstanceInfo> taskCountColumn = new TextColumn<InstanceInfo>() {
    @Override
    public String getValue(InstanceInfo instanceInfo) {
      long taskMaxDurationSeconds = simulationInfo.getConfig().getTaskMaxDurationInSeconds();
      return String.valueOf(MechaverseManagerUtil.getActiveTasks(
          instanceInfo.getExecutingTasks(), taskMaxDurationSeconds).size());
    }
  };

  private final TextColumn<InstanceInfo> clientColumn = new TextColumn<InstanceInfo>() {
    @Override
    public String getValue(InstanceInfo instanceInfo) {
      return instanceInfo.getPreferredClientId();
    }
  };

  @UiField Label simulationIdLabel;

  @UiField(provided = true)
  CellTable<InstanceInfo> instanceTable = new CellTable<>(DEFAULT_PAGE_SIZE, TableResources.INSTANCE);

  @UiField(provided = true)
  EditSimulationConfigView editConfigView = new EditSimulationConfigView();

  private final ListDataProvider<InstanceInfo> dataProvider = new ListDataProvider<>();
  private final Button saveConfigButton = new ActionButton("SAVE");

  private SimulationInfo simulationInfo;

  private Observer observer;

  public SimulationInfoView(
      WebConsoleLayoutView layoutView, PlaceHistoryMapper placeHistoryMapper) {
    initWidget(uiBinder.createAndBindUi(this));

    dataProvider.addDataDisplay(instanceTable);
    instanceTable.setEmptyTableWidget(new Label("No instances"));

    saveConfigButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (observer != null) {
          observer.updateConfig(editConfigView.getSimulationConfig());
        }
      }
    });

    initInstanceInfoTable();

    instanceTable.addCellPreviewHandler(new CellPreviewEvent.Handler<InstanceInfo>() {
      @Override
      public void onCellPreview(CellPreviewEvent<InstanceInfo> event) {
        if (BrowserEvents.CLICK.equals(event.getNativeEvent().getType()) && observer != null) {
          observer.onSelectInstance(event.getValue());
        }
      }
    });

    RefreshButton refreshButton = new RefreshButton();
    refreshButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (observer != null) {
          observer.onRefresh();
        }
      }
    });

    layoutView.setNavWidget(BasicNavMenu.newBuilder(placeHistoryMapper)
      .addLink(ManagerDashboardPlace.NAME, new ManagerDashboardPlace())
      .build());

    layoutView.addActionButton(saveConfigButton);
    layoutView.addActionButton(refreshButton);
  }

  public void setObserver(Observer observer) {
    this.observer = observer;
  }

  public void setSimulationInfo(SimulationInfo simulationInfo) {
    this.simulationInfo = simulationInfo;

    simulationIdLabel.setText("Simulation " + simulationInfo.getSimulationId());
    dataProvider.setList(new ArrayList<>(simulationInfo.getInstances()));
    editConfigView.setSimulationConfig(simulationInfo.getConfig());
  }

  private void initInstanceInfoTable() {
    idColumn.setSortable(true);
    instanceTable.addColumn(idColumn, "Id");
    iterationColumn.setSortable(true);
    instanceTable.addColumn(iterationColumn, "Iteration");
    taskCountColumn.setSortable(true);
    instanceTable.addColumn(taskCountColumn, "Executing tasks");
    clientColumn.setSortable(true);
    instanceTable.addColumn(clientColumn, "Preferred client");
  }
}
