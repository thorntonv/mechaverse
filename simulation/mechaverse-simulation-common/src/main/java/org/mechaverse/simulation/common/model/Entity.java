package org.mechaverse.simulation.common.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Entity", namespace = "http://www.mechaverse.org/simulation/api/model",
    propOrder = {"id", "x", "y", "energy", "maxEnergy", "age", "direction"})
public abstract class Entity implements Serializable {

  private final static long serialVersionUID = -1L;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/api/model")
  protected String id;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/api/model")
  private int x;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/api/model")
  private int y;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/api/model")
  private int energy;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/api/model")
  private int maxEnergy;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/ant/api/model")
  private long age;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/api/model")
  protected Direction direction;

  public Entity() {}

  /**
   * Gets the value of the id property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the value of the id property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setId(String value) {
    this.id = value;
  }

  /**
   * Gets the value of the x property.
   */
  public int getX() {
    return x;
  }

  /**
   * Sets the value of the x property.
   */
  public void setX(int value) {
    this.x = value;
  }

  /**
   * Gets the value of the y property.
   */
  public int getY() {
    return y;
  }

  /**
   * Sets the value of the y property.
   */
  public void setY(int value) {
    this.y = value;
  }

  /**
   * Gets the value of the energy property.
   */
  public int getEnergy() {
    return energy;
  }

  /**
   * Sets the value of the energy property.
   */
  public void setEnergy(int value) {
    this.energy = value;
  }

  /**
   * Gets the value of the maxEnergy property.
   */
  public int getMaxEnergy() {
    return maxEnergy;
  }

  /**
   * Sets the value of the maxEnergy property.
   */
  public void setMaxEnergy(int value) {
    this.maxEnergy = value;
  }

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

  /**
   * Gets the value of the direction property.
   *
   * @return possible object is
   * {@link Direction }
   */
  public Direction getDirection() {
    return direction;
  }

  /**
   * Sets the value of the direction property.
   *
   * @param value allowed object is
   *              {@link Direction }
   */
  public void setDirection(Direction value) {
    this.direction = value;
  }

}
