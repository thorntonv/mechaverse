package org.mechaverse.simulation.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SimulationModel<
    ENV_MODEL extends EnvironmentModel<ENT_MODEL, ENT_TYPE>,
    ENT_MODEL extends EntityModel<ENT_TYPE>,
    ENT_TYPE extends Enum<ENT_TYPE>> implements Serializable {

  protected String id;
  protected ENV_MODEL environment;
  private List<ENV_MODEL> subEnvironments;
  protected long iteration;
  private String seed;
  @JsonProperty
  private Map<String, byte[]> data = new HashMap<>();

  public SimulationModel() {}

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
   * {@link EnvironmentModel }
   */
  public ENV_MODEL getEnvironment() {
    return environment;
  }

  /**
   * Sets the value of the environment property.
   *
   * @param value allowed object is
   *              {@link EnvironmentModel }
   */
  public void setEnvironment(ENV_MODEL value) {
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
   * {@link EnvironmentModel }
   */
  public List<ENV_MODEL> getSubEnvironments() {
    if (subEnvironments == null) {
      subEnvironments = new ArrayList<>();
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

  public byte[] getData(String key) {
    return data.get(key);
  }

  public void putData(String key, byte[] value) {
    data.put(key, value);
  }

  public boolean dataContainsKey(String key) {
    return data.containsKey(key);
  }

  @JsonIgnore
  public ENV_MODEL getEnvironment(String environmentId) {
    if (environmentId == null) {
      return null;
    }
    ENV_MODEL env = getEnvironment();
    if (env != null && environmentId.equalsIgnoreCase(env.getId())) {
      return env;
    }
    for (ENV_MODEL subEnvironment : getSubEnvironments()) {
      if (subEnvironment != null && environmentId.equalsIgnoreCase(subEnvironment.getId())) {
        return subEnvironment;
      }
    }
    return null;
  }

  /**
   * @return a list of the environments in the given state
   */
  @JsonIgnore
  public Collection<ENV_MODEL> getEnvironments() {
    List<ENV_MODEL> environments = Lists.newArrayList();
    environments.add(getEnvironment());
    environments.addAll(getSubEnvironments());
    return environments;
  }
}
