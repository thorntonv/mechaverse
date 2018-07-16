package org.mechaverse.simulation.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.util.ArrayList;
import java.util.List;


@JsonTypeInfo(use=Id.NAME)
public abstract class EnvironmentModel<
    ENT_MODEL extends EntityModel<ENT_TYPE>,
    ENT_TYPE extends Enum<ENT_TYPE>> {

  private String id;
  private int width;
  private int height;
  private List<ENT_MODEL> entities;

  public EnvironmentModel() {}

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
   * Gets the value of the width property.
   */
  public int getWidth() {
    return width;
  }

  /**
   * Sets the value of the width property.
   */
  public void setWidth(int value) {
    this.width = value;
  }

  /**<SimulationModel, AntSimulationState>
   * Gets the value of the height property.
   */
  public int getHeight() {
    return height;
  }

  /**
   * Sets the value of the height property.
   */
  public void setHeight(int value) {
    this.height = value;
  }

  /**
   * Gets the value of the entities property.
   * <p>
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the entities property.
   * <p>
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getEntities().add(newItem);
   * </pre>
   * <p>
   * <p>
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link EntityModel }
   */
  public List<ENT_MODEL> getEntities() {
    if (entities == null) {
      entities = new ArrayList<>();
    }
    return this.entities;
  }

  @JsonIgnore
  public abstract ENT_TYPE[] getEntityTypes();
}
