package org.mechaverse.service.manager.api.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Information about a simulation instance.
 */
@Entity
@XmlRootElement
@Table(name = "instance")
public class InstanceInfo implements Serializable {

  private static final long serialVersionUID = -6809364169796167082L;

  @Id
  private String instanceId;

  private long iteration;

  private String preferredClientId;

  @OneToMany(fetch=FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Task> executingTasks = new HashSet<>();

  @XmlID
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

  public void setExecutingTasks(Set<Task> executingTasks) {
    this.executingTasks.clear();
    this.executingTasks.addAll(executingTasks);
  }

  public String getPreferredClientId() {
    return preferredClientId;
  }

  public void setPreferredClientId(String preferredClientId) {
    this.preferredClientId = preferredClientId;
  }
}
