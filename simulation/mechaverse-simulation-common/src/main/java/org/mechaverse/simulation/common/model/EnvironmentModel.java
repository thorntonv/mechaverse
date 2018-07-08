package org.mechaverse.simulation.common.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EnvironmentModel", namespace = "http://www.mechaverse.org/simulation/api/model",
    propOrder = {"id", "width", "height", "entities"})
@XmlRootElement(name = "EnvironmentModel", namespace = "http://www.mechaverse.org/simulation/api/model")
public class EnvironmentModel implements Serializable {

  private final static long serialVersionUID = -1L;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/api/model", required = true)
  protected String id;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/api/model")
  protected int width;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/api/model")
  protected int height;
  @XmlElement(name = "entity", namespace = "http://www.mechaverse.org/simulation/api/model")
  protected List<EntityModel> entities;

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
  public List<EntityModel> getEntities() {
    if (entities == null) {
      entities = new ArrayList<>();
    }
    return this.entities;
  }

}
