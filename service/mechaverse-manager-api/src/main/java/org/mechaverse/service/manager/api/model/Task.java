package org.mechaverse.service.manager.api.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Information about a task.
 */
@Entity
@XmlRootElement
@Table(name = "task")
public class Task {

  @Id
  @XmlID
  @XmlElement
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Long id;

  @XmlElement private String simulationId;
  @XmlElement private String instanceId;
  @XmlElement private long iteration;
  @XmlElement private int iterationCount;
  @XmlElement private String clientId;
  @XmlElement private Timestamp startTime;
  @XmlElement private Timestamp completionTime;

  public Timestamp getStartTime() {
    return startTime;
  }

  public void setStartTime(Timestamp startTime) {
    this.startTime = startTime;
  }

  public Timestamp getCompletionTime() {
    return completionTime;
  }

  public void setCompletionTime(Timestamp completionTime) {
    this.completionTime = completionTime;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getSimulationId() {
    return simulationId;
  }

  public void setSimulationId(String simulationId) {
    this.simulationId = simulationId;
  }

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

  public int getIterationCount() {
    return iterationCount;
  }

  public void setIterationCount(int iterationCount) {
    this.iterationCount = iterationCount;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }
}
