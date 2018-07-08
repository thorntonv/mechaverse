package org.mechaverse.simulation.common;

import java.util.Map;
import java.util.Map.Entry;

import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCell;
import org.mechaverse.simulation.common.cellautomaton.environment.AbstractCellEnvironment;
import org.mechaverse.simulation.common.model.Entity;

public abstract class AbstractActiveEntityProviders<M, S extends SimulationState<M>, T extends Enum<T>,
    C extends AbstractCell<T>, E extends AbstractCellEnvironment<T, C>,
    A extends ActiveEntity<M, S, T, C, E>, P extends AbstractActiveEntityProvider<M, S, T, C, E, A>> {

    private final P[] activeEntityProviders;
    private final EntityFactory<T> entityFactory;

    public AbstractActiveEntityProviders(Map<T, P> activeEntityProviderMap, EntityFactory<T> entityFactory) {
        this.entityFactory = entityFactory;
        this.activeEntityProviders = createActiveEntityProviderArray(entityFactory.getTypeValues().length);
        for (Entry<T, P> entry : activeEntityProviderMap.entrySet()) {
            activeEntityProviders[entry.getKey().ordinal()] = entry.getValue();
        }
    }

    public P get(Entity entity) {
        return get(entityFactory.getType(entity));
    }

    public P get(T entityType) {
        return activeEntityProviders[entityType.ordinal()];
    }

    protected abstract P[] createActiveEntityProviderArray(int length);
}
