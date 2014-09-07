package org.mechaverse.service.manager.api.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Simulation configuration information.
 */
@Entity
@XmlRootElement
@Table(name = "config")
public class SimulationConfig implements Serializable {

  private static final long serialVersionUID = -3040420226275039589L;

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Long id;

  private int minInstanceCount;
  private int maxInstanceCount;
  private int taskIterationCount;
  private long taskMaxDurationInSeconds;

  @OneToMany(fetch=FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<SimulationConfigProperty> configProperties = new HashSet<>();

  public int getMinInstanceCount() {
    return minInstanceCount;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public void setMinInstanceCount(int minInstanceCount) {
    this.minInstanceCount = minInstanceCount;
  }

  public int getMaxInstanceCount() {
    return maxInstanceCount;
  }

  public void setMaxInstanceCount(int maxInstanceCount) {
    this.maxInstanceCount = maxInstanceCount;
  }

  public int getTaskIterationCount() {
    return taskIterationCount;
  }

  public void setTaskIterationCount(int taskIterationCount) {
    this.taskIterationCount = taskIterationCount;
  }

  public long getTaskMaxDurationInSeconds() {
    return taskMaxDurationInSeconds;
  }

  public void setTaskMaxDurationInSeconds(long taskMaxDurationInSeconds) {
    this.taskMaxDurationInSeconds = taskMaxDurationInSeconds;
  }

  public Set<SimulationConfigProperty> getConfigProperties() {
    return configProperties;
  }

  public void setConfigProperties(Set<SimulationConfigProperty> properties) {
    this.configProperties.clear();
    this.configProperties.addAll(configProperties);
  }
}
