package org.mechaverse.simulation.common.cellautomaton.examples;

import java.util.function.Function;
import java.util.function.Supplier;
import com.google.common.collect.Sets;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.mechaverse.simulation.common.cellautomaton.simulation.AbstractCellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.EightNeighborCellConnector;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonRenderer;
import org.mechaverse.simulation.common.cellautomaton.ui.CellularAutomatonVisualizer;
import org.mechaverse.simulation.common.model.Direction;
import org.mechaverse.simulation.common.util.RandomUtil;

import java.awt.*;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * A cellular automaton with a number of entities. An entity can be associated with other entities.
 * Entities move randomly, but if an entity is part of an association its movement will be
 * restricted so that the associated entities form a connected component. An entity will join an
 * association if it has at least one neighbor that is part of the association.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
@SuppressWarnings("WeakerAccess")
public class Association extends AbstractCellularAutomaton {

  private static final int ROW_COUNT = 108;
  private static final int COL_COUNT = 192;
  private static final double ENTITY_PROBABILITY = .05;
  private static final int MAX_DEPTH = 10;
  private static final double ASSOCIATION_PROBABILITY = .01;
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
  
  private static class AssociationCell extends AbstractCell {
    
    private IdentityHashMap<Cell, Entity> cellEntityMap; 
    private static final RandomGenerator random = new Well19937c();
    
    public AssociationCell(IdentityHashMap<Cell, Entity> cellEntityMap) {
      this.outputs = new int[1];
      this.cellEntityMap = cellEntityMap;
            
      if (RandomUtil.nextEvent(ENTITY_PROBABILITY, random)) {
        Entity entity = new Entity();
        if (RandomUtil.nextEvent(ASSOCIATION_PROBABILITY, random)) {
          entity.setAssociationId(DEFAULT_ASSOCIATION_ID);
        }
        setEntity(entity);
      }
    }

    public void setEntity(Entity entity) {
      cellEntityMap.put(this, entity);
    }

    @Override
    public void beforeUpdate() {
      Entity entity = getEntity();

      if (entity != null && entity.getMoveDirection() != null) {
        Cell targetNeighborCell = neighbors[entity.getMoveDirection().ordinal()];
        if (cellEntityMap.get(targetNeighborCell) == null) {
          cellEntityMap.remove(this);
          entity.setMoveDirection(null);

          for (Cell neighborCell : neighbors) {
            Entity neighborEntity = cellEntityMap.get(neighborCell);
            if (neighborEntity != null && neighborEntity.getAssociationId() != null) {
              entity.setAssociationId(neighborEntity.getAssociationId());
            }
          }
          
          boolean associationConstraintSatisfied = entity.getAssociationId() == null;
          if (!associationConstraintSatisfied) {
            associationConstraintSatisfied = true;
            for (Cell neighborCell : neighbors) {
              if (neighborCell != null && neighborCell != targetNeighborCell
                  && neighborCell instanceof AssociationCell
                  && cellEntityMap.containsKey(neighborCell)) {
                if (!((AssociationCell) neighborCell).isConnected(
                    targetNeighborCell, entity.getAssociationId())) {
                  associationConstraintSatisfied = false;
                }
              }
            }
          }

          if (associationConstraintSatisfied) {
            cellEntityMap.put(targetNeighborCell, entity);
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
        if (neighborCell instanceof AssociationCell
            && cellEntityMap.containsKey(neighborCell)) {
          neighborCount++;
        }
      }
      return neighborCount;
    }

    protected boolean isConnected(Cell cell, String association) {
      return isConnected(cell, association, Sets.newIdentityHashSet(), 0);
    }

    protected boolean isConnected(Cell cell, String association, Set<Cell> visited, int depth) {
      Entity entity = getEntity();

      if (depth > MAX_DEPTH) {
        return false;
      }
      if (this == cell) {
        return true;
      }

      if (entity == null || entity.getAssociationId() == null
          || !entity.getAssociationId().equals(association)) {
        return false;
      }

      visited.add(this);
      for (Cell neighbor : getNeighbors()) {
        if (!visited.contains(neighbor) && neighbor instanceof AssociationCell) {
          if (((AssociationCell) neighbor).isConnected(cell, association, visited, depth+1)) {
            return true;
          }
        }
      }
      return false;
    }
    
    public void update() {
      Entity entity = getEntity();
      outputs[0] = entity == null ? 0 : 1;
      if (entity != null && entity.getAssociationId() != null) {
        outputs[0] = 2;
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
 
  private static final Function<Cell, Color> CELL_COLOR_PROVIDER = cell -> {
    switch (cell.getOutput(0)) {
      case 0:
        return Color.BLACK;
      case 1:
        return Color.WHITE;
      default:
        return Color.GREEN;
    }
  };

  public Association(int width, int height) {
    this(width, height, new IdentityHashMap<>());
  }
  
  protected Association(int width, int height, final IdentityHashMap<Cell, Entity> cellEntityMap) {
    super(width, height, (Supplier<AssociationCell>) () -> new AssociationCell(cellEntityMap),
        new EightNeighborCellConnector());
    
    AssociationCell cell = getCell(getHeight() / 2, getWidth() /2);
    Entity entity = new Entity();
    entity.setAssociationId(DEFAULT_ASSOCIATION_ID);
    cell.setEntity(entity);
  }

  public AssociationCell getCell(int row, int column) {
    return (AssociationCell) super.getCell(row, column);
  }
  
  public static void main(String[] args) throws IOException {
    CellularAutomatonCLI cli = new CellularAutomatonCLI() {

      @Override
      protected CellularAutomaton createCellularAutomaton() {
        return new Association(COL_COUNT, ROW_COUNT);
      }

      @Override
      protected CellularAutomatonRenderer createCellularAutomatonRenderer(CellularAutomaton cells,
          int width, int height) {
        return new CellularAutomatonRenderer(cells, CELL_COLOR_PROVIDER, width, height);
      }

      @Override
      protected CellularAutomatonVisualizer createVisualizer(int width, int height,
          int framesPerSecond, int frameCount) {
        return new CellularAutomatonVisualizer(createCellularAutomaton(), CELL_COLOR_PROVIDER,
            width, height, framesPerSecond, frameCount);
      }
    };
    CellularAutomatonCLI.main(args, cli);
  }
}
