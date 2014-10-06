package org.mechaverse.service.storage;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mechaverse.simulation.common.SimulationDataStore;

import com.mongodb.MongoClient;

public class MongoDBMechaverseStorageServiceTest {

  private MongoDBMechaverseStorageService service;
  
  boolean compareSimulationDataStore(SimulationDataStore storeA, SimulationDataStore storeB)
  {
    if(storeA.size() != storeB.size())
    {
      return false;
    }
    
    for(String key : storeA.keySet())
    {
      if(!Arrays.equals(storeA.get(key), storeB.get(key)))
      {
        return false;
      }
    }
    
    return true;
  }
  
  @Before
  public void before() throws IOException
  {
    MongoClient mongoClient = new MongoClient("localhost", 27017);
    mongoClient.getDB("mechaverse-storage-test").dropDatabase();;
    mongoClient.close();
    
    this.service = new MongoDBMechaverseStorageService();
    this.service.setMongoDatabase("mechaverse-storage-test");
  }
  
  @After
  public void after() throws IOException
  {
    MongoClient mongoClient = new MongoClient("localhost", 27017);
    mongoClient.getDB("mechaverse-storage-test").dropDatabase();;
    mongoClient.close();
  }
  
  @Test
  public void testState() throws IOException
  {
    SimulationDataStore setStore = new SimulationDataStore();
    setStore.put("key1", "value1".getBytes());
    setStore.put("key2", "value2".getBytes());
    setStore.put("key3", "value3".getBytes());
    InputStream setStream = new ByteArrayInputStream(setStore.serialize());
    
    this.service.setState("simulation-id", "instance-id", 0, setStream);
    
    InputStream getStream;
    getStream = this.service.getState("simulation-id", "instance-id", 0);
    SimulationDataStore getStore = SimulationDataStore.deserialize(getStream);
    
    assertTrue(compareSimulationDataStore(setStore, getStore));
  }
  
  @Test
  public void testDeleteSimulation() throws IOException
  {
    SimulationDataStore store = new SimulationDataStore();
    store.put("key1", "value1".getBytes());
    store.put("key2", "value2".getBytes());
    store.put("key3", "value3".getBytes());
    
    this.service.setState("simulation1", "instance1", 0, new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation1", "instance1", 1, new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation1", "instance2", 0, new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation1", "instance2", 1, new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation2", "instance1", 0, new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation2", "instance1", 1, new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation2", "instance2", 0, new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation2", "instance2", 1, new ByteArrayInputStream(store.serialize()));

    this.service.deleteSimulation("simulation2");
    
    assertTrue(this.service.getState("simulation1", "instance1", 0) instanceof InputStream);
    assertTrue(this.service.getState("simulation1", "instance1", 1) instanceof InputStream);
    assertTrue(this.service.getState("simulation1", "instance2", 0) instanceof InputStream);
    assertTrue(this.service.getState("simulation1", "instance2", 1) instanceof InputStream);
    
    boolean pass = false;
    try
    {
      this.service.getState("simulation2", "instance1", 0);
    }
    catch(IOException ex)
    {
      pass = true;
    }
    assertTrue(pass);
    
    pass = false;
    try
    {
      this.service.getState("simulation2", "instance1", 1);
    }
    catch(IOException ex)
    {
      pass = true;
    }
    assertTrue(pass);

    pass = false;
    try
    {
      this.service.getState("simulation2", "instance2", 0);
    }
    catch(IOException ex)
    {
      pass = true;
    }
    assertTrue(pass);
    
    pass = false;
    try
    {
      this.service.getState("simulation2", "instance2", 1);
    }
    catch(IOException ex)
    {
      pass = true;
    }
    assertTrue(pass);
  }
  
  @Test
  public void testDeleteInstance() throws IOException
  {
    SimulationDataStore store = new SimulationDataStore();
    store.put("key1", "value1".getBytes());
    store.put("key2", "value2".getBytes());
    store.put("key3", "value3".getBytes());
    
    this.service.setState("simulation1", "instance1", 0, new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation1", "instance1", 1, new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation1", "instance2", 0, new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation1", "instance2", 1, new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation2", "instance1", 0, new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation2", "instance1", 1, new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation2", "instance2", 0, new ByteArrayInputStream(store.serialize()));
    this.service.setState("simulation2", "instance2", 1, new ByteArrayInputStream(store.serialize()));

    this.service.deleteInstance("simulation2", "instance2");
    
    assertTrue(this.service.getState("simulation1", "instance1", 0) instanceof InputStream);
    assertTrue(this.service.getState("simulation1", "instance1", 1) instanceof InputStream);
    assertTrue(this.service.getState("simulation1", "instance2", 0) instanceof InputStream);
    assertTrue(this.service.getState("simulation1", "instance2", 1) instanceof InputStream);
    assertTrue(this.service.getState("simulation2", "instance1", 0) instanceof InputStream);
    assertTrue(this.service.getState("simulation2", "instance1", 1) instanceof InputStream);

    boolean pass = false;
    try
    {
      this.service.getState("simulation2", "instance2", 0);
    }
    catch(IOException ex)
    {
      pass = true;
    }
    assertTrue(pass);
    
    pass = false;
    try
    {
      this.service.getState("simulation2", "instance2", 1);
    }
    catch(IOException ex)
    {
      pass = true;
    }
    assertTrue(pass);
  }
}
