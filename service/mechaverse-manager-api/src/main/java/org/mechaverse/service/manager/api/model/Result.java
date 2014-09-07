package org.mechaverse.service.manager.api.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents the result of a task execution.
 */
@Entity
@XmlRootElement
@Table(name = "result")
public class Result {

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Long id;

  @OneToOne
  private Task task;

  private String temporaryDataFilename;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Task getTask() {
    return task;
  }

  public void setTask(Task task) {
    this.task = task;
  }

  public String getTemporaryDataFilename() {
    return temporaryDataFilename;
  }

  public void setTemporaryDataFilename(String temporaryDataFilename) {
    this.temporaryDataFilename = temporaryDataFilename;
  }
}
