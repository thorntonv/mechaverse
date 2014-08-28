package org.mechaverse.service.manager.api.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A single simulation configuration property. Property values are set on new instances based on
 * configuration.
 */
@Entity
@XmlRootElement
@Table(name = "configproperty")
public class SimulationConfigProperty {

  @Id
  @XmlID
  @XmlElement
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Long id;

  @XmlElement private String name;
  @XmlElement private byte[] value;

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
