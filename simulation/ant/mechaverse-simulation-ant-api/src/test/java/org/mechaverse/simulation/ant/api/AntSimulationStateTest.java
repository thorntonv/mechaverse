package org.mechaverse.simulation.ant.api;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.common.SimulationDataStore;

import com.google.common.collect.ImmutableSet;

/**
 * Unit test for {@link AntSimulationState}.
 */
public class AntSimulationStateTest {

  private static final String KEY1 = "key1";
  private static final byte[] DATA1 = "data1".getBytes();

  private static final String KEY2 = "key2";
  private static final byte[] DATA2 = "data2".getBytes();

  @Test
  public void putAndGetEntityValue() {
    AntSimulationState state = new AntSimulationState();
    Entity entity = new Entity();
    entity.setId("001");
    state.putEntityValue(entity, KEY1, DATA1);
    String entityKey = "entity.001." + KEY1;
    assertTrue(state.containsKey(entityKey));
    assertEquals(keySetWithEntityKeys(entityKey), state.keySet());
    assertArrayEquals(DATA1, state.getData(entityKey));
    assertArrayEquals(DATA1, state.getEntityValue(entity, KEY1));
  }

  @Test
  public void putAndGetEntityValues() {
    AntSimulationState state = new AntSimulationState();
    Entity entity = new Entity();
    entity.setId("001");

    SimulationDataStore entityDataStore = new SimulationDataStore();
    entityDataStore.put(KEY1, DATA1);
    entityDataStore.put(KEY2, DATA2);

    state.putEntityValues(entity, entityDataStore);

    String entityKey1 = "entity.001." + KEY1;
    String entityKey2 = "entity.001." + KEY2;

    assertTrue(state.containsKey(entityKey1));
    assertTrue(state.containsKey(entityKey2));
    assertEquals(keySetWithEntityKeys(entityKey1, entityKey2), state.keySet());
    assertArrayEquals(DATA1, state.getData(entityKey1));
    assertArrayEquals(DATA2, state.getData(entityKey2));

    state.setData("test", "test".getBytes());

    entityDataStore = state.getEntityValues(entity);
    assertEquals(ImmutableSet.of(KEY1, KEY2), entityDataStore.keySet());
  }

  @Test
  public void removeAllEntityValues() {
    AntSimulationState state = new AntSimulationState();
    Entity entity1 = new Entity();
    entity1.setId("001");
    SimulationDataStore entityDataStore = new SimulationDataStore();
    entityDataStore.put(KEY1, DATA1);
    entityDataStore.put(KEY2, DATA2);
    state.putEntityValues(entity1, entityDataStore);

    Entity entity2 = new Entity();
    entity2.setId("002");
    entityDataStore = new SimulationDataStore();
    entityDataStore.put(KEY1, DATA1);
    state.putEntityValues(entity2, entityDataStore);

    state.setData("test", "test".getBytes());

    state.removeAllEntityValues();

    assertEquals(keySetWithEntityKeys("test"), state.keySet());
  }

  private Set<String> keySetWithEntityKeys(String... entityKeys) {
    HashSet<String> keySet = new HashSet<String>(Arrays.asList(entityKeys));
    keySet.add(AntSimulationState.CONFIG_KEY);
    keySet.add(AntSimulationState.MODEL_KEY);
    return keySet;
  }
}
