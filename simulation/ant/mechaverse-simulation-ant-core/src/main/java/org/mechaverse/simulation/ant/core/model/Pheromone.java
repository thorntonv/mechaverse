package org.mechaverse.simulation.ant.core.model;

import org.mechaverse.simulation.common.model.EntityModel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Pheromone", namespace = "http://www.mechaverse.org/simulation/ant/api/model", propOrder = {"value"})
public class Pheromone extends EntityModel implements Serializable {

  private final static long serialVersionUID = -1L;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/ant/api/model")
  private int value;

  public Pheromone() {}

  /**
   * Gets the value of the value property.
   */
  public int getValue() {
    return value;
  }

  /**
   * Sets the value of the value property.
   */
  public void setValue(int value) {
    this.value = value;
  }

}
