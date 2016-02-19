package org.mechaverse.gwt.client.manager;

import java.util.List;

import org.mechaverse.gwt.client.manager.ManagerDashboardPresenter.ManagerDashboardPlace;
import org.mechaverse.gwt.common.client.webconsole.ActionButton;
import org.mechaverse.gwt.common.client.webconsole.BasicNavMenu;
import org.mechaverse.gwt.common.client.webconsole.RefreshButton;
import org.mechaverse.gwt.common.client.webconsole.WebConsoleLayoutView;
import org.mechaverse.gwt.common.client.webconsole.WebConsoleResourceBundle.TableResources;
import org.mechaverse.service.manager.api.MechaverseManagerUtil;
import org.mechaverse.service.manager.api.model.InstanceInfo;
import org.mechaverse.service.manager.api.model.SimulationInfo;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ListDataProvider;

/**
 * A view that shows a list of running simulations.
 */
public class ManagerDashboardView extends Composite {

  public interface Observer {

    void onCreateSimulation();
    void onRefresh();
    void onDeleteSimulation(SimulationInfo simulationInfo);
    void onSelectSimulation(SimulationInfo selectedSimulationInfo);
  }

  interface MyUiBinder extends UiBinder<Widget, ManagerDashboardView> {}

  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final int DEFAULT_PAGE_SIZE = 20;

  private final TextColumn<SimulationInfo> nameColumn = new TextColumn<SimulationInfo>() {
    @Override
    public String getValue(SimulationInfo simulationInfo) {
      return simulationInfo.getName();
    }
  };

  private final TextColumn<SimulationInfo> idColumn = new TextColumn<SimulationInfo>() {
    @Override
    public String getValue(SimulationInfo simulationInfo) {
      return simulationInfo.getSimulationId();
    }
  };

  private final TextColumn<SimulationInfo> instanceCountColumn = new TextColumn<SimulationInfo>() {
    @Override
    public String getValue(SimulationInfo simulationInfo) {
      return String.valueOf(simulationInfo.getInstances().size());
    }
  };

  private final TextColumn<SimulationInfo> taskCountColumn = new TextColumn<SimulationInfo>() {
    @Override
    public String getValue(SimulationInfo simulationInfo) {
      int taskCount = 0;
      for (InstanceInfo instanceInfo : simulationInfo.getInstances()) {
        long taskMaxDurationSeconds = simulationInfo.getConfig().getTaskMaxDurationInSeconds();
        taskCount += MechaverseManagerUtil.getActiveTasks(
          instanceInfo.getExecutingTasks(), taskMaxDurationSeconds).size();
      }
      return String.valueOf(taskCount);
    }
  };

  private final TextColumn<SimulationInfo> activeColumn = new TextColumn<SimulationInfo>() {
    @Override
    public String getValue(SimulationInfo simulationInfo) {
      return simulationInfo.isActive() ? "active" : "inactive";
    }
  };

  private final Column<SimulationInfo, String> deleteColumn =
      new Column<SimulationInfo, String>(new ButtonCell()) {
          @Override
          public String getValue(SimulationInfo simulationInfo) {
            return "x";
          }
      };

  @UiField(provided = true) CellTable<SimulationInfo> simulationInfoTable =
      new CellTable<>(DEFAULT_PAGE_SIZE, TableResources.INSTANCE);

  private final ListDataProvider<SimulationInfo> dataProvider = new ListDataProvider<>();
  private final Button createSimulationButton = new ActionButton("CREATE SIMULATION");
  private Observer observer;

  public ManagerDashboardView(
      WebConsoleLayoutView layoutView, PlaceHistoryMapper placeHistoryMapper) {
    initWidget(uiBinder.createAndBindUi(this));

    layoutView.addActionButton(createSimulationButton);

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

    layoutView.addActionButton(refreshButton);

    initSimulationTable();
  }

  public void setSimulationInfo(List<SimulationInfo> simulationInfoList) {
    dataProvider.setList(simulationInfoList);
  }

  public void setObserver(Observer observer) {
    this.observer = observer;
  }

  private void initSimulationTable() {
    nameColumn.setSortable(true);
    simulationInfoTable.addColumn(nameColumn, "Name");

    idColumn.setSortable(true);
    simulationInfoTable.addColumn(idColumn, "Id");

    instanceCountColumn.setSortable(true);
    simulationInfoTable.addColumn(instanceCountColumn, "Instances");

    taskCountColumn.setSortable(true);
    simulationInfoTable.addColumn(taskCountColumn, "Running tasks");

    activeColumn.setSortable(true);
    simulationInfoTable.addColumn(activeColumn, "Active");

    simulationInfoTable.addColumn(deleteColumn);

    simulationInfoTable.setEmptyTableWidget(new Label("No simulations"));

    deleteColumn.setFieldUpdater(new FieldUpdater<SimulationInfo, String>() {
      @Override
      public void update(int idx, SimulationInfo simulationInfo, String title) {
        if (observer != null) {
          observer.onDeleteSimulation(simulationInfo);
        }
      }
    });

    createSimulationButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (observer != null) {
          observer.onCreateSimulation();
        }
      }
    });

    simulationInfoTable.addCellPreviewHandler(new CellPreviewEvent.Handler<SimulationInfo>() {
      @Override
      public void onCellPreview(CellPreviewEvent<SimulationInfo> event) {
        if (event.getColumn() == simulationInfoTable.getColumnIndex(deleteColumn)) {
          // Ignore clicks to the delete column.
          return;
        }
        if (BrowserEvents.CLICK.equals(event.getNativeEvent().getType()) && observer != null) {
          observer.onSelectSimulation(event.getValue());
        }
      }
    });

    dataProvider.addDataDisplay(simulationInfoTable);
  }
}
