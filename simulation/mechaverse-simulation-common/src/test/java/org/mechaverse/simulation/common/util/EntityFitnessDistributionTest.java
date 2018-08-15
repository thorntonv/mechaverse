package org.mechaverse.simulation.common.util;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mechaverse.simulation.common.model.EntityModel;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EntityFitnessDistributionTest {

    private enum EntityType {
        ENTITY
    }

    private static class TestEntityModel extends EntityModel<EntityType> {
        @Override
        public EntityType getType() {
            return EntityType.ENTITY;
        }
    }

    private static TestEntityModel ENTITY1 = createEntityModel(10);
    private static TestEntityModel ENTITY2 = createEntityModel(25);
    private static TestEntityModel ENTITY3 = createEntityModel(3);
    private static TestEntityModel ENTITY4 = createEntityModel(4);
    private static TestEntityModel ENTITY5 = createEntityModel(6);

    @Mock
    private RandomGenerator mockRandomGenerator;

    @Test
    public void selectEntity() {
        TestEntityModel[] entities = new TestEntityModel[]{ENTITY1, ENTITY2, ENTITY3, ENTITY4, ENTITY5};

        EntityFitnessDistribution<TestEntityModel, EntityType> distribution = new EntityFitnessDistribution<>(entities,
                entity -> entity.getAge() > 5 ? entity.getAge() : 0.0);

        when(mockRandomGenerator.nextDouble()).thenReturn(0.0);
        assertEquals(ENTITY1, distribution.selectEntity(mockRandomGenerator));

        when(mockRandomGenerator.nextDouble()).thenReturn(0.20);
        assertEquals(ENTITY1, distribution.selectEntity(mockRandomGenerator));

        when(mockRandomGenerator.nextDouble()).thenReturn(0.5);
        assertEquals(ENTITY2, distribution.selectEntity(mockRandomGenerator));

        when(mockRandomGenerator.nextDouble()).thenReturn(0.75);
        assertEquals(ENTITY2, distribution.selectEntity(mockRandomGenerator));

        when(mockRandomGenerator.nextDouble()).thenReturn(.9);
        assertEquals(ENTITY5, distribution.selectEntity(mockRandomGenerator));

        when(mockRandomGenerator.nextDouble()).thenReturn(1.0);
        assertEquals(ENTITY5, distribution.selectEntity(mockRandomGenerator));
    }

    @Test
    public void selectEntity_noEntities() {
        TestEntityModel[] entities = new TestEntityModel[]{};

        EntityFitnessDistribution<TestEntityModel, EntityType> distribution = new EntityFitnessDistribution<>(entities,
                entity -> entity.getAge() > 5 ? entity.getAge() : 0.0);
        when(mockRandomGenerator.nextDouble()).thenReturn(0.5);
        assertNull(distribution.selectEntity(mockRandomGenerator));
    }

    @Test
    public void selectEntity_noFitEntities() {
        TestEntityModel[] entities = new TestEntityModel[]{ENTITY1, ENTITY2, ENTITY3, ENTITY4, ENTITY5};

        EntityFitnessDistribution<TestEntityModel, EntityType> distribution =
                new EntityFitnessDistribution<>(entities, entity -> 0.0);
        when(mockRandomGenerator.nextDouble()).thenReturn(0.5);
        assertNull(distribution.selectEntity(mockRandomGenerator));
    }

    private static TestEntityModel createEntityModel(int age) {
        TestEntityModel model = new TestEntityModel();
        model.setAge(age);
        return model;
    }
}