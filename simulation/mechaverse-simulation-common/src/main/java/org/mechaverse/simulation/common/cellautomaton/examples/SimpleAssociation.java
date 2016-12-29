package org.mechaverse.simulation.common.cellautomaton.examples;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.Pair;
import org.mechaverse.simulation.common.cellautomaton.simulation.AbstractCellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.EightNeighborCellConnector;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonRenderer;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonVisualizer;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.util.RandomUtil;

import java.awt.*;
import java.io.IOException;
import java.util.*;

/**
 * A cellular automaton with a number of entities. An entity can be associated with other entities.
 * Entities move randomly, but if an entity is part of an association its movement will be
 * restricted so that the associated entities form a connected component. An entity will join an
 * association if it has at least one neighbor that is part of the association.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class SimpleAssociation extends AbstractCellularAutomaton {

  private static final int ROW_COUNT = 108*2;
  private static final int COL_COUNT = 192*2;
  private static final double ENTITY_PROBABILITY = .1;
  private static final double ASSOCIATION_PROBABILITY = .001;
  private static final String DEFAULT_ASSOCIATION_ID = "1";

  private static final class Entity {

    private Direction moveDirection;
    private String associationId;

    public Direction getMoveDirection() {
      return moveDirection;
    }

    public void setMoveDirection(Direction moveDirection) {
      this.moveDirection = moveDirection;
    }

    public String getAssociationId() {
      return associationId;
    }

    public void setAssociationId(String associationId) {
      this.associationId = associationId;
    }
  }

  private static final class AssociationCentroid {

    private int cellCount;
    private double rowSum;
    private double colSum;

    public int getCellCount() {
      return cellCount;
    }

    public double getRow() {
      return rowSum / cellCount;
    }

    public double getCol() {
      return colSum / cellCount;
    }

    public void addCell(int row, int col) {
      rowSum += row;
      colSum += col;
      cellCount++;
    }

    public void moveCell(int fromRow, int fromCol, int toRow, int toCol) {
      rowSum = rowSum - fromRow + toRow;
      colSum = colSum - fromCol + toCol;
    }

    public void removeCell(Integer row, Integer col) {
      rowSum -= row;
      colSum -= col;
      cellCount--;
    }
  }

  private static class AssociationCell extends AbstractCell {

    private IdentityHashMap<Cell, Entity> cellEntityMap;
    private Map<String, AssociationCentroid> associationCentroidMap;
    private SimpleAssociation cells;

    private final RandomGenerator random = new Well19937c();

    public AssociationCell(IdentityHashMap<Cell, Entity> cellEntityMap,
                           Map<String, AssociationCentroid> associationCentroidMap) {
      this.outputs = new int[1];
      this.cellEntityMap = cellEntityMap;
      this.associationCentroidMap = associationCentroidMap;
    }

    private void init(SimpleAssociation cells) {
      this.cells = cells;
      if (RandomUtil.nextEvent(ENTITY_PROBABILITY, random)) {
        Entity entity = new Entity();
        setEntity(entity);
        if (RandomUtil.nextEvent(ASSOCIATION_PROBABILITY, random)) {
          createNewAssociation();
        }
      }
    }

    public void createNewAssociation() {
      Entity entity = getEntity();
      String associationId = UUID.randomUUID().toString();
      entity.setAssociationId(associationId);
      AssociationCentroid centroid = associationCentroidMap.get(associationId);
      if(centroid == null) {
        centroid = new AssociationCentroid();
        associationCentroidMap.put(associationId, centroid);
      }
      Pair<Integer, Integer> position = cells.getCellPosition(this);
      centroid.addCell(position.getFirst(), position.getSecond());
    }
    public void setEntity(Entity entity) {
      cellEntityMap.put(this, entity);
    }

    @Override
    public int getParam(String name) {
      return 0;
    }

    @Override
    public void setParam(String name, int value) {}

    @Override
    public Collection<String> getParamNames() {
      return Collections.emptyList();
    }

    @Override
    public int getOutputParam(String name, int outputIndex) {
      return 0;
    }

    @Override
    public void setOutputParam(String name, int outputIndex, int value) {}

    @Override
    public Collection<String> getOutputParamNames(int outputIndex) {
      return Collections.emptyList();
    }

    @Override
    public void beforeUpdate() {
      Entity entity = getEntity();

      if (entity != null && entity.getMoveDirection() != null) {
        AbstractCell targetNeighborCell = (AbstractCell) neighbors[entity.getMoveDirection().ordinal()];
        if (cellEntityMap.get(targetNeighborCell) == null) {
          cellEntityMap.remove(this);
          entity.setMoveDirection(null);

          AssociationCentroid centroid = null;
          if(entity.getAssociationId() != null) {
            centroid = associationCentroidMap.get(entity.getAssociationId());

            for (Cell neighborCell : neighbors) {
              Entity neighborEntity = cellEntityMap.get(neighborCell);
              if (neighborEntity != null) {
                boolean infectious = false;

                if(infectious || neighborEntity.getAssociationId() == null) {
                  Pair<Integer, Integer> neighborPosition = cells.getCellPosition(neighborCell);
                  if(neighborEntity.getAssociationId() != null) {
                    AssociationCentroid oldCentroid = associationCentroidMap.get(neighborEntity.getAssociationId());
                    oldCentroid.removeCell(neighborPosition.getFirst(), neighborPosition.getSecond());
                  }
                  neighborEntity.setAssociationId(entity.getAssociationId());
                  centroid.addCell(neighborPosition.getFirst(), neighborPosition.getSecond());
                }
              }
            }
          }

          boolean associationConstraintSatisfied = entity.getAssociationId() == null;
          if (!associationConstraintSatisfied) {
            associationConstraintSatisfied = true;
            if (!isConnected(targetNeighborCell, centroid)) {
              associationConstraintSatisfied = false;
            }
          }

          if (associationConstraintSatisfied) {
            cellEntityMap.put(targetNeighborCell, entity);
            if(centroid != null) {
              Pair<Integer, Integer> fromCell = cells.getCellPosition(this);
              Pair<Integer, Integer> toCell = cells.getCellPosition(targetNeighborCell);
              centroid.moveCell(fromCell.getFirst(), fromCell.getSecond(), toCell.getFirst(), toCell.getSecond());
            }
          } else {
            // Undo the move.
            cellEntityMap.put(this, entity);
          }
        }
      }
    }

    @SuppressWarnings("unused")
    protected int getNeighborCount() {
      int neighborCount = 0;
      for (Cell neighborCell : neighbors) {
        if (neighborCell != null && neighborCell instanceof AssociationCell
            && cellEntityMap.containsKey(neighborCell)) {
          neighborCount++;
        }
      }
      return neighborCount;
    }

    protected boolean isConnected(AbstractCell cell, AssociationCentroid centroid) {
      Pair<Integer, Integer> position = cells.getCellPosition(this);
      Pair<Integer, Integer> targetPosition = cells.getCellPosition(cell);
      double row = position.getFirst();
      double col = position.getSecond();
      double targetRow = targetPosition.getFirst();
      double targetCol = targetPosition.getSecond();
      double centroidRow = centroid.getRow();
      double centroidCol = centroid.getCol();

      if((col <= centroidCol && targetCol > col) || (col > centroidCol && targetCol < col)) {
        if((row <= centroidRow && targetRow > row) || (row > centroidRow && targetRow < row)) {
          return true;
        }
      }

      double distance =
          Math.pow(targetRow - centroidRow, 2) + Math.pow(targetCol - centroidCol, 2);

//      System.out.println("row:" + row +
//          ", col: " + col +
//          ", targetRow: " + targetRow +
//          ", targetCol: " + targetCol +
//          ", centroidRow: " + centroidRow +
//          ", centroidCol: " + centroidCol +
//          ", disance: " + distance);
      if(distance <= (centroid.getCellCount() * 2.5d) / 3.14159d) {
        return true;
      }

      if (RandomUtil.nextEvent(.5, random)) {
        return true;
      }

      return false;
    }

    public void update() {
      Entity entity = getEntity();
      outputs[0] = entity == null ? 0 : 1;
      if (entity != null && entity.getAssociationId() != null) {
        outputs[0] = entity.getAssociationId().hashCode();
      }
      if (entity != null) {
        entity.setMoveDirection(randomDirection());
      }
    }

    public Entity getEntity() {
      return cellEntityMap.get(this);
    }

    private Direction randomDirection() {
      return Direction.values()[random.nextInt(Direction.values().length)];
    }
  }

  private static Map<String, Color> associationColorMap = new HashMap<>();

  private static final Function<Cell, Color> CELL_COLOR_PROVIDER = new Function<Cell, Color>() {
    @Override
    public Color apply(Cell cell) {
      AssociationCell associationCell = (AssociationCell) cell;
      if(associationCell.getEntity() == null) {
        return Color.BLACK;
      }
      String associationId = associationCell.getEntity().getAssociationId();
      if(associationId == null) {
        return Color.WHITE;
      }

      Color color = associationColorMap.get(associationId);
      if(color == null) {
        Random random = new Random();
        color = Color.getHSBColor(random.nextFloat(), 1.0f, 1.0f);
        associationColorMap.put(associationId, color);
      }
      return color;
    }
  };

  public SimpleAssociation(int width, int height) {
    this(width, height, new IdentityHashMap<Cell, Entity>(), new HashMap<String, AssociationCentroid>());
  }

  protected SimpleAssociation(int width, int height, final IdentityHashMap<Cell, Entity> cellEntityMap,
                              final Map<String, AssociationCentroid> associationCentroidMap) {
    super(width, height, new Supplier<AssociationCell>() {
      @Override
      public AssociationCell get() {
        return new AssociationCell(cellEntityMap, associationCentroidMap);
      }
    }, new EightNeighborCellConnector());

    for(int row = 0; row < getHeight(); row++) {
      for(int col = 0; col < getWidth(); col++) {
        AssociationCell cell = getCell(row, col);
        cell.init(this);
      }
    }
    AssociationCell cell = getCell(getHeight() / 2, getWidth() /2);
    Entity entity = new Entity();
    entity.setAssociationId(DEFAULT_ASSOCIATION_ID);
    cell.setEntity(entity);
    cell.createNewAssociation();
  }

  public AssociationCell getCell(int row, int column) {
    return (AssociationCell) super.getCell(row, column);
  }

  public static void main(String[] args) throws IOException {
    CellularAutomatonCLI cli = new CellularAutomatonCLI() {

      @Override
      protected CellularAutomaton createCellularAutomaton() throws IOException {
        return new SimpleAssociation(COL_COUNT, ROW_COUNT);
      }

      @Override
      protected CellularAutomatonRenderer createCellularAutomatonRenderer(CellularAutomaton cells,
          int width, int height) {
        return new CellularAutomatonRenderer(cells, CELL_COLOR_PROVIDER, width, height);
      }

      @Override
      protected CellularAutomatonVisualizer createVisualizer(int width, int height,
          int framesPerSecond, int frameCount) throws IOException {
        return new CellularAutomatonVisualizer(createCellularAutomaton(), CELL_COLOR_PROVIDER,
            width, height, framesPerSecond, frameCount);
      }
    };
    CellularAutomatonCLI.main(args, cli);
  }
}
