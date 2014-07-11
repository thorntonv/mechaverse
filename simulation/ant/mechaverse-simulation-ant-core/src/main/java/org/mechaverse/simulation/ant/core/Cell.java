package org.mechaverse.simulation.ant.core;

import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Barrier;
import org.mechaverse.simulation.ant.api.model.Conduit;
import org.mechaverse.simulation.ant.api.model.Dirt;
import org.mechaverse.simulation.ant.api.model.Food;
import org.mechaverse.simulation.ant.api.model.Nest;
import org.mechaverse.simulation.ant.api.model.Pheromone;
import org.mechaverse.simulation.ant.api.model.Rock;

public final class Cell {

  private final int row;
  private final int column;

  private Ant ant;
  private Barrier barrier;
  private Conduit conduit;
  private Dirt dirt;
  private Food food;
  private Nest nest;
  private Pheromone pheromone;
  private Rock rock;

  public Cell(int row, int column) {
    this.row = row;
    this.column = column;
  }

  public int getRow() {
    return row;
  }

  public int getColumn() {
    return column;
  }

  public Ant getAnt() {
    return ant;
  }

  public void setAnt(Ant ant) {
    this.ant = ant;
  }

  public Barrier getBarrier() {
    return barrier;
  }

  public void setBarrier(Barrier barrier) {
    this.barrier = barrier;
  }

  public Conduit getConduit() {
    return conduit;
  }

  public void setConduit(Conduit conduit) {
    this.conduit = conduit;
  }

  public Dirt getDirt() {
    return dirt;
  }

  public void setDirt(Dirt dirt) {
    this.dirt = dirt;
  }

  public Food getFood() {
    return food;
  }

  public void setFood(Food food) {
    this.food = food;
  }

  public Nest getNest() {
    return nest;
  }

  public void setNest(Nest nest) {
    this.nest = nest;
  }

  public Pheromone getPheromone() {
    return pheromone;
  }

  public void setPheromone(Pheromone pheromone) {
    this.pheromone = pheromone;
  }

  public Rock getRock() {
    return rock;
  }

  public void setRock(Rock rock) {
    this.rock = rock;
  }

  public boolean isEmpty() {
    return ant == null && barrier == null && conduit == null && dirt == null && food == null
        && nest == null && pheromone == null && rock == null;
  }
}
