package org.mechaverse.simulation.ant.core.model;

import org.mechaverse.simulation.common.model.Entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Food", namespace = "http://www.mechaverse.org/simulation/ant/api/model")
public class Food extends Entity implements Serializable {

  private final static long serialVersionUID = -1L;

  public Food() {}
}
