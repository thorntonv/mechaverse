package org.mechaverse.gwt.client;

import org.mechaverse.gwt.common.client.util.ImageUtil;
import org.mechaverse.simulation.ant.api.model.Direction;
import org.mechaverse.simulation.ant.api.model.EntityType;

import com.google.common.collect.ImmutableMap;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * A bundle which contains common resources.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public interface AntSimulationResourceBundle extends ClientBundle {

  public static final AntSimulationResourceBundle INSTANCE =
      GWT.create(AntSimulationResourceBundle.class);

  public static final ImageElement BARRIER_IMAGE_ELEMENT =
      ImageUtil.asImageElement(INSTANCE.barrier());
  public static final ImageElement BLACK_ANT_EAST_IMAGE_ELEMENT =
      ImageUtil.asImageElement(INSTANCE.blackAntEast());
  public static final ImageElement BLACK_ANT_NORTH_EAST_IMAGE_ELEMENT =
      ImageUtil.asImageElement(INSTANCE.blackAntNorthEast());
  public static final ImageElement BLACK_ANT_NORTH_IMAGE_ELEMENT =
      ImageUtil.asImageElement(INSTANCE.blackAntNorth());
  public static final ImageElement BLACK_ANT_NORTH_WEST_IMAGE_ELEMENT =
      ImageUtil.asImageElement(INSTANCE.blackAntNorthWest());
  public static final ImageElement BLACK_ANT_WEST_IMAGE_ELEMENT =
      ImageUtil.asImageElement(INSTANCE.blackAntWest());
  public static final ImageElement BLACK_ANT_SOUTH_WEST_IMAGE_ELEMENT =
      ImageUtil.asImageElement(INSTANCE.blackAntSouthWest());
  public static final ImageElement BLACK_ANT_SOUTH_IMAGE_ELEMENT =
      ImageUtil.asImageElement(INSTANCE.blackAntSouth());
  public static final ImageElement BLACK_ANT_SOUTH_EAST_IMAGE_ELEMENT =
      ImageUtil.asImageElement(INSTANCE.blackAntSouthEast());
  public static final ImageElement CONDUIT_IMAGE_ELEMENT =
      ImageUtil.asImageElement(INSTANCE.conduit());
  public static final ImageElement DIRT_IMAGE_ELEMENT =
      ImageUtil.asImageElement(INSTANCE.dirt());
  public static final ImageElement FOOD_IMAGE_ELEMENT =
      ImageUtil.asImageElement(INSTANCE.food());
  public static final ImageElement NEST_IMAGE_ELEMENT =
      ImageUtil.asImageElement(INSTANCE.nest());
  public static final ImageElement PHEROMONE_IMAGE_ELEMENT =
      ImageUtil.asImageElement(INSTANCE.pheromone());
  public static final ImageElement RED_ANT_IMAGE_ELEMENT =
      ImageUtil.asImageElement(INSTANCE.redAntEast());
  public static final ImageElement ROCK_IMAGE_ELEMENT =
      ImageUtil.asImageElement(INSTANCE.rock());

  public static final ImmutableMap<EntityType, ImageResource> ENTITY_IMAGES =
      ImmutableMap.<EntityType, ImageResource>builder()
          .put(EntityType.ANT, INSTANCE.blackAntEast())
          .put(EntityType.BARRIER, INSTANCE.barrier())
          .put(EntityType.CONDUIT, INSTANCE.conduit())
          .put(EntityType.DIRT, INSTANCE.dirt())
          .put(EntityType.FOOD, INSTANCE.food())
          .put(EntityType.NEST, INSTANCE.nest())
          .put(EntityType.PHEROMONE, INSTANCE.pheromone())
          .put(EntityType.ROCK, INSTANCE.rock())
          .build();

  public static final ImmutableMap<EntityType, ImageElement> ENTITY_IMAGE_ELEMENTS =
      ImmutableMap.<EntityType, ImageElement>builder()
          .put(EntityType.ANT, BLACK_ANT_EAST_IMAGE_ELEMENT)
          .put(EntityType.BARRIER, BARRIER_IMAGE_ELEMENT)
          .put(EntityType.CONDUIT, CONDUIT_IMAGE_ELEMENT)
          .put(EntityType.DIRT, DIRT_IMAGE_ELEMENT)
          .put(EntityType.FOOD, FOOD_IMAGE_ELEMENT)
          .put(EntityType.NEST, NEST_IMAGE_ELEMENT)
          .put(EntityType.PHEROMONE, PHEROMONE_IMAGE_ELEMENT)
          .put(EntityType.ROCK, ROCK_IMAGE_ELEMENT)
          .build();

  public static final ImmutableMap<Direction, ImageElement> BLACK_ANT_IMAGE_ELEMENTS =
      ImmutableMap.<Direction, ImageElement>builder()
          .put(Direction.EAST, BLACK_ANT_EAST_IMAGE_ELEMENT)
          .put(Direction.NORTH_EAST, BLACK_ANT_NORTH_EAST_IMAGE_ELEMENT)
          .put(Direction.NORTH, BLACK_ANT_NORTH_IMAGE_ELEMENT)
          .put(Direction.NORTH_WEST, BLACK_ANT_NORTH_WEST_IMAGE_ELEMENT)
          .put(Direction.WEST, BLACK_ANT_WEST_IMAGE_ELEMENT)
          .put(Direction.SOUTH_WEST, BLACK_ANT_SOUTH_WEST_IMAGE_ELEMENT)
          .put(Direction.SOUTH, BLACK_ANT_SOUTH_IMAGE_ELEMENT)
          .put(Direction.SOUTH_EAST, BLACK_ANT_SOUTH_EAST_IMAGE_ELEMENT)
          .build();

  @Source("images/barrier.png")
  ImageResource barrier();

  @Source("images/black-ant-east.png")
  ImageResource blackAntEast();

  @Source("images/black-ant-northEast.png")
  ImageResource blackAntNorthEast();

  @Source("images/black-ant-north.png")
  ImageResource blackAntNorth();

  @Source("images/black-ant-northWest.png")
  ImageResource blackAntNorthWest();

  @Source("images/black-ant-west.png")
  ImageResource blackAntWest();

  @Source("images/black-ant-southWest.png")
  ImageResource blackAntSouthWest();

  @Source("images/black-ant-south.png")
  ImageResource blackAntSouth();

  @Source("images/black-ant-southEast.png")
  ImageResource blackAntSouthEast();

  @Source("images/conduit.png")
  ImageResource conduit();

  @Source("images/dirt.png")
  ImageResource dirt();

  @Source("images/food.png")
  ImageResource food();

  @Source("images/nest.png")
  ImageResource nest();

  @Source("images/pheromone.png")
  ImageResource pheromone();

  @Source("images/red-ant-east.png")
  ImageResource redAntEast();

  @Source("images/rock.png")
  ImageResource rock();

  public static interface Style extends CssResource {

    String entityToolbar();
    String entityButton();
    String entityButtonSelected();

    String environmentPanel();
  }

  @Source("mechaverse.css")
  public Style css();
}
