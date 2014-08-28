package org.mechaverse.service.manager.api.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Information about a simulation.
 */
@Entity
@XmlRootElement
@Table(name = "simulation")
public class SimulationInfo {

  @Id
  @XmlID
  @XmlElement
  private String simulationId;

  @OneToMany(fetch=FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true)
  @XmlElement
  private Set<InstanceInfo> instances = new HashSet<>();

  @OneToOne(fetch=FetchType.EAGER, cascade = CascadeType.ALL)
  @XmlElement
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

  public SimulationConfig getConfig() {
    return config;
  }

  public void setConfig(SimulationConfig config) {
    this.config = config;
  }
}
