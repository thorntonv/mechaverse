package org.mechaverse.service.manager.api.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Information about a task.
 */
@Entity
@XmlRootElement
@Table(name = "task")
public class Task implements Serializable {

  private static final long serialVersionUID = 5958546141000173564L;

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Long id;

  private String simulationId;
  private String instanceId;
  private long iteration;
  private int iterationCount;
  private String clientId;
  private Date startTime;
  private Date completionTime;

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getCompletionTime() {
    return completionTime;
  }

  public void setCompletionTime(Date completionTime) {
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
