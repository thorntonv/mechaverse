package org.mechaverse.gwt.client.environment;

import org.mechaverse.gwt.client.environment.CellEnvironment.Cell;
import org.mechaverse.gwt.client.util.UUID;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Barrier;
import org.mechaverse.simulation.ant.api.model.Direction;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.Food;
import org.mechaverse.simulation.ant.api.model.Rock;

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

  @Override
  public void onCellClick(int row, int column) {
    Cell cell = cells.getCell(row, column);

    if (cell.getEntities().isEmpty()) {
      Entity newEntity = null;
      switch (view.getToolbar().getSelectedEntityType()) {
        case ANT:
          newEntity = new Ant();
          newEntity.setId(UUID.uuid().toString());
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

  @Override
  public Widget asWidget() {
    return view;
  }
}
