package org.mechaverse.simulation.ant.core.model;

import org.mechaverse.simulation.common.model.Entity;

import javax.xml.bind.annotation.*;
import java.io.Serializable;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Ant", namespace = "http://www.mechaverse.org/simulation/ant/api/model",
    propOrder = {"carriedEntity"})
@XmlRootElement(name = "Ant", namespace = "http://www.mechaverse.org/simulation/ant/api/model")
public class Ant extends Entity implements Serializable {

  private final static long serialVersionUID = -1L;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/ant/api/model")
  private Entity carriedEntity;

  public Ant() {}

  /**
   * Gets the value of the carriedEntity property.
   *
   * @return possible object is
   * {@link Entity }
   */
  public Entity getCarriedEntity() {
    return carriedEntity;
  }

  /**
   * Sets the value of the carriedEntity property.
   *
   * @param value allowed object is
   *              {@link Entity }
   */
  public void setCarriedEntity(Entity value) {
    this.carriedEntity = value;
  }

}
