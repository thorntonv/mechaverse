package org.mechaverse.gwt.client.environment;

import org.mechaverse.api.model.simulation.ant.Ant;
import org.mechaverse.api.model.simulation.ant.Barrier;
import org.mechaverse.api.model.simulation.ant.Direction;
import org.mechaverse.api.model.simulation.ant.Entity;
import org.mechaverse.api.model.simulation.ant.Environment;
import org.mechaverse.api.model.simulation.ant.Food;
import org.mechaverse.api.model.simulation.ant.Rock;
import org.mechaverse.gwt.client.environment.CellEnvironment.Cell;
import org.mechaverse.gwt.client.util.UUID;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * A presenter for {@link EnvironmentEditorView}.
 * 
 * @author thorntonv@mechaverse.org
 */
public class EnvironmentEditorPresenter implements EnvironmentView.Observer, IsWidget {

  private CellEnvironment cells;
  private final EnvironmentEditorView view;

  public EnvironmentEditorPresenter(EnvironmentEditorView view) {
    this.view = view;

    view.getEnvironmentView().addObserver(this);
  }

  public void setEnvironment(Environment environment) {
    this.cells = new CellEnvironment(environment);
    view.setEnvironment(environment);
    view.getEnvironmentView().update();
  }
  
  public void onCellClick(int row, int column) {
    Cell cell = cells.getCell(row, column);

    if (cell.getEntities().isEmpty()) {
      Entity newEntity = null;
      switch (view.getToolbar().getSelectedEntityType()) {
        case ANT:
          newEntity = new Ant();
          newEntity.setDirection(Direction.EAST);
          break;
        case BARRIER:
          newEntity = new Barrier();
          break;
        case FOOD:
          newEntity = new Food();
          break;
        case ROCK:
          newEntity = new Rock();
          break;
        default:
          break;
      }

      if (newEntity != null) {
        newEntity.setId(UUID.uuid().toString());
        cell.add(newEntity);
      }
    }

    view.getEnvironmentView().update();
  }

  @Override
  public void onCellAltClick(int row, int column) {
    Cell cell = cells.getCell(row, column);
    cell.clear();

    view.getEnvironmentView().update();
  }

  public Widget asWidget() {
    return view;
  }
}
