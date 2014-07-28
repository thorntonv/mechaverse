package org.mechaverse.gwt.client.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mechaverse.gwt.client.MechaverseResourceBundle;
import org.mechaverse.gwt.client.util.Coordinate;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Direction;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.Rock;
import org.mechaverse.simulation.ant.api.util.EntityUtil;

import com.google.common.collect.Sets;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * A view which displays an environment.
 *
 * @author thorntonv@mechaverse.org
 */
public class EnvironmentView extends SimplePanel {

  public static interface Observer {

    void onCellClick(int row, int column);
    void onCellAltClick(int row, int column);
  }

  private static final EntityType[] ENTITY_TYPE_DRAW_ORDER = {EntityType.DIRT, EntityType.BARRIER,
      EntityType.PHEROMONE, EntityType.CONDUIT, EntityType.NEST, EntityType.ROCK, EntityType.FOOD,
      EntityType.ANT};

  private int cellWidth = MechaverseResourceBundle.INSTANCE.dirt().getWidth();
  private int cellHeight = MechaverseResourceBundle.INSTANCE.dirt().getHeight();

  private Canvas canvas;
  private Environment environment;
  private Set<Coordinate> dirtyCells = Sets.newHashSet();
  private Set<Observer> observers = Sets.newHashSet();

  public EnvironmentView() {
    addStyleName(MechaverseResourceBundle.INSTANCE.css().environmentPanel());
    this.canvas = Canvas.createIfSupported();

    add(canvas);
    canvas.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int row = event.getY() / cellHeight;
        int column = event.getX() / cellWidth;

        for (Observer observer : observers) {
          if(event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
            observer.onCellClick(row, column);
          } else {
            observer.onCellAltClick(row, column);
          }
        }
      }
    });
  }

  public void setEnvironment(Environment environment) {
    this.environment = environment;
    canvas.setCoordinateSpaceWidth(cellWidth * environment.getWidth());
    canvas.setCoordinateSpaceHeight(cellHeight * environment.getHeight());

    dirtyCells.clear();
    for (int row = 0; row < environment.getHeight(); row++) {
      for (int column = 0; column < environment.getWidth(); column++) {
        dirtyCells.add(Coordinate.create(row, column));
      }
    }

    update();
  }

  public HandlerRegistration addObserver(final Observer observer) {
    observers.add(observer);
    return new HandlerRegistration() {
      @Override
      public void removeHandler() {
        observers.remove(observer);
      }
    };
  }

  public void update() {
    Context2d context = canvas.getContext2d();

    for (Coordinate coordinate : dirtyCells) {
      drawImage(MechaverseResourceBundle.DIRT_IMAGE_ELEMENT, context, coordinate.getRow(),
          coordinate.getColumn());
    }
    dirtyCells.clear();

    List<Entity> carriedEntities = new ArrayList<>();
    for (Entity entity : environment.getEntities()) {
      if (entity instanceof Ant) {
        Ant ant = (Ant) entity;
        if (ant.getCarriedEntity() != null) {
          carriedEntities.add(ant.getCarriedEntity());
        }
      }
    }

    for (EntityType entityType : ENTITY_TYPE_DRAW_ORDER) {
      for (Entity entity : environment.getEntities()) {
        if (EntityUtil.getType(entity) == entityType) {
          ImageElement image = getImage(entity);
          drawEntity(entity, image, context);
        }
      }
    }

    for (Entity entity : carriedEntities) {
      ImageElement image = getImage(entity);
      double scale = .85;
      if(entity instanceof Rock) {
        scale = .5;
      }
      drawEntity(entity, image, scale, context);
    }
  }

  private ImageElement getImage(Entity entity) {
    EntityType entityType = EntityUtil.getType(entity);
    ImageElement image = MechaverseResourceBundle.ENTITY_IMAGE_ELEMENTS.get(entityType);
    if (entityType == EntityType.ANT) {
      Direction direction = entity.getDirection();
      direction = direction != null ? direction : Direction.EAST;
      image = MechaverseResourceBundle.BLACK_ANT_IMAGE_ELEMENTS.get(direction);
    }
    return image;
  }

  protected void drawEntity(Entity entity, ImageElement image, Context2d context) {
    double scale = 1;
    drawEntity(entity, image, scale, context);
  }

  protected void drawEntity(Entity entity, ImageElement image, double scale, Context2d context) {
    Coordinate coord = Coordinate.create(entity.getY(), entity.getX());
    drawImage(image, context, coord.getRow(), coord.getColumn(), scale);
    dirtyCells.add(coord);
  }

  protected void drawImage(ImageElement image, Context2d context, int row, int column) {
    drawImage(image, context, row, column, 1);
  }

  protected void drawImage(
      ImageElement image, Context2d context, int row, int column, double scale) {
    int width = (int) (image.getWidth() * scale);
    int height = (int) (image.getHeight() * scale);
    int xOffset = (cellWidth - width) / 2;
    int yOffset = (cellHeight - height) / 2;
    context.drawImage(image, column * cellWidth + xOffset, row * cellHeight + yOffset,
        width, height);
  }
}
