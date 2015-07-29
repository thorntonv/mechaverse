package org.mechaverse.simulation.common.genetic.selection;

/**
 * Common base {@link SelectionStrategy} implementation.
 */
public abstract class AbstractSelectionStrategy<E> implements SelectionStrategy<E> {

  protected boolean minimize = false;

  @Override
  public void setMinimize(boolean minimize) {
    this.minimize = minimize;
  }

  @Override
  public boolean getMinimize() {
    return minimize;
  }
}
