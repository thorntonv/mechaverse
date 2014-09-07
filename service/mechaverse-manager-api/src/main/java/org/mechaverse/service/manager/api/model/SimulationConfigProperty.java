package org.mechaverse.service.manager.api.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A single simulation configuration property. Property values are set on new instances based on
 * configuration.
 */
@Entity
@XmlRootElement
@Table(name = "configproperty")
public class SimulationConfigProperty implements Serializable {

  private static final long serialVersionUID = 8902502314352164640L;

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Long id;

  private String name;
  private byte[] value;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public byte[] getValue() {
    return value;
  }

  public void setValue(byte[] value) {
    this.value = value;
  }
}
