package org.mechaverse.manager.service.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Information about a task.
 */
@Entity
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
  private Long startTimeMillis;
  private Long completionTimeMillis;

  public Long getStartTimeMillis() {
    return startTimeMillis;
  }

  public void setStartTimeMillis(Long startTimeMillis) {
    this.startTimeMillis = startTimeMillis;
  }

  public Long getCompletionTimeMillis() {
    return completionTimeMillis;
  }

  public void setCompletionTimeMillis(Long completionTimeMillis) {
    this.completionTimeMillis = completionTimeMillis;
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
