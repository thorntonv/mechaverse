package org.mechaverse.simulation.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


@JsonTypeInfo(use=Id.NAME)
public abstract class EntityModel<ENT_TYPE extends Enum<ENT_TYPE>> implements Serializable {

  protected String id;
  private int x;
  private int y;
  private int energy;
  private int maxEnergy;
  private long age;
  private Direction direction;

  @JsonProperty
  private Map<String, byte[]> data = new HashMap<>();

  public EntityModel() {}

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
  public final int getX() {
    return x;
  }

  /**
   * Sets the value of the x property.
   */
  public final void setX(int value) {
    this.x = value;
  }

  /**
   * Gets the value of the y property.
   */
  public final int getY() {
    return y;
  }

  /**
   * Sets the value of the y property.
   */
  public final void setY(int value) {
    this.y = value;
  }

  /**
   * Gets the value of the energy property.
   */
  public final int getEnergy() {
    return energy;
  }

  /**
   * Sets the value of the energy property.
   */
  public final void setEnergy(int value) {
    this.energy = value;
  }

  /**
   * Gets the value of the maxEnergy property.
   */
  public final int getMaxEnergy() {
    return maxEnergy;
  }

  /**
   * Sets the value of the maxEnergy property.
   */
  public final void setMaxEnergy(int value) {
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

  public byte[] getData(String key) {
    return data.get(key);
  }

  public void putData(String key, byte[] value) {
    data.put(key, value);
  }

  public void removeData(String key) {
    data.remove(key);
  }

  public void clearData() {
    data.clear();
  }

  public boolean dataContainsKey(String key) {
    return data.containsKey(key);
  }

  @JsonIgnore
  public abstract ENT_TYPE getType();

}
