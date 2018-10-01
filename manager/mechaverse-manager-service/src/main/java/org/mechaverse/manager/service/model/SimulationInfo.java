package org.mechaverse.manager.service.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Information about a simulation.
 */
@Entity
@Table(name = "simulation")
public class SimulationInfo implements Serializable {

  private static final long serialVersionUID = 159877921410539157L;

  @Id
  private String simulationId;

  private String name;
  private boolean active;

  @OneToMany(fetch=FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true)
  private Set<InstanceInfo> instances = new HashSet<>();

  @OneToOne(fetch=FetchType.EAGER, cascade = CascadeType.ALL)
  private SimulationConfig config;

  public String getSimulationId() {
    return simulationId;
  }

  public void setSimulationId(String simulationId) {
    this.simulationId = simulationId;
  }

  public Set<InstanceInfo> getInstances() {
    return instances;
  }

  public void setInstances(Set<InstanceInfo> instances) {
    this.instances.clear();
    this.instances.addAll(instances);
  }

  public SimulationConfig getConfig() {
    return config;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public void setConfig(SimulationConfig config) {
    this.config = config;
  }
}
