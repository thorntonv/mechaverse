package org.mechaverse.simulation.common.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SimulationModel", namespace = "http://www.mechaverse.org/simulation/api/model",
    propOrder = {"id", "environment", "subEnvironments", "iteration", "seed"})
@XmlRootElement(name = "SimulationModel", namespace = "http://www.mechaverse.org/simulation/api/model")
public class SimulationModel implements Serializable {

  private final static long serialVersionUID = -1L;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/api/model", required = true)
  protected String id;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/api/model", required = true)
  protected Environment environment;
  @XmlElement(name = "sub_environment", namespace = "http://www.mechaverse.org/simulation/api/model")
  protected List<Environment> subEnvironments;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/api/model")
  protected long iteration;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/api/model", required = true)
  protected String seed;

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
   * Gets the value of the environment property.
   *
   * @return possible object is
   * {@link Environment }
   */
  public Environment getEnvironment() {
    return environment;
  }

  /**
   * Sets the value of the environment property.
   *
   * @param value allowed object is
   *              {@link Environment }
   */
  public void setEnvironment(Environment value) {
    this.environment = value;
  }

  /**
   * Gets the value of the subEnvironments property.
   * <p>
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the subEnvironments property.
   * <p>
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getSubEnvironments().add(newItem);
   * </pre>
   * <p>
   * <p>
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link Environment }
   */
  public List<Environment> getSubEnvironments() {
    if (subEnvironments == null) {
      subEnvironments = new ArrayList<Environment>();
    }
    return this.subEnvironments;
  }

  /**
   * Gets the value of the iteration property.
   */
  public long getIteration() {
    return iteration;
  }

  /**
   * Sets the value of the iteration property.
   */
  public void setIteration(long value) {
    this.iteration = value;
  }

  /**
   * Gets the value of the seed property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getSeed() {
    return seed;
  }

  /**
   * Sets the value of the seed property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setSeed(String value) {
    this.seed = value;
  }

}
