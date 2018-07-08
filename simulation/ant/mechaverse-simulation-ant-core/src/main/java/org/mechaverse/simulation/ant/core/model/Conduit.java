package org.mechaverse.simulation.ant.core.model;

import org.mechaverse.simulation.common.model.Entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Conduit", namespace = "http://www.mechaverse.org/simulation/ant/api/model",
    propOrder = {"targetEnvironmentId"})
public class Conduit extends Entity implements Serializable {

  private final static long serialVersionUID = -1L;
  @XmlElement(namespace = "http://www.mechaverse.org/simulation/ant/api/model", required = true)
  private String targetEnvironmentId;

  public Conduit() {}

  /**
   * Gets the value of the targetEnvironmentId property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getTargetEnvironmentId() {
    return targetEnvironmentId;
  }

  /**
   * Sets the value of the targetEnvironmentId property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setTargetEnvironmentId(String value) {
    this.targetEnvironmentId = value;
  }

}
