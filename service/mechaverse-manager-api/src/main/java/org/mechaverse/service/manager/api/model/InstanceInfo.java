package org.mechaverse.service.manager.api.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Information about a simulation instance.
 */
@Entity
@XmlRootElement
@Table(name = "instance")
public class InstanceInfo {

  @Id
  @XmlID
  @XmlElement
  private String instanceId;

  @XmlElement
  private long iteration;

  @XmlElement
  private String preferredClientId;

  @OneToMany(fetch=FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @XmlElement
  private Set<Task> executingTasks = new HashSet<>();

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public long getIteration() {
    return iteration;
  }

  public void setIteration(long iteration) {
    this.iteration = iteration;
  }

  public Set<Task> getExecutingTasks() {
    return executingTasks;
  }

  public String getPreferredClientId() {
    return preferredClientId;
  }

  public void setPreferredClientId(String preferredClientId) {
    this.preferredClientId = preferredClientId;
  }
}
