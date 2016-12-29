package org.mechaverse.simulation.primordial.core.model;

import org.mechaverse.simulation.common.model.Entity;

import javax.xml.bind.annotation.*;
import java.io.Serializable;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PrimordialEntity", namespace = "http://www.mechaverse.org/simulation/primordial/api/model",
    propOrder = {"age"})
@XmlRootElement(name = "PrimordialEntity", namespace = "http://www.mechaverse.org/simulation/primordial/api/model")
public class PrimordialEntity extends Entity implements Serializable {

  private final static long serialVersionUID = -1L;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/primordial/api/model")
  protected long age;

  /**
   *
   *
   */
  @XmlAttribute(name = "type")
  public final static EntityType TYPE = EntityType.ENTITY;

  public PrimordialEntity() {}

  /**
   * Gets the value of the age property.
   */
  public long getAge() {
    return age;
  }

  /**
   * Sets the value of the age property.
   */
  public void setAge(long value) {
    this.age = value;
  }
}
