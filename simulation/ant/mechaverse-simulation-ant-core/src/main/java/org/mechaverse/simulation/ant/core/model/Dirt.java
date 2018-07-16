package org.mechaverse.simulation.ant.core.model;

import org.mechaverse.simulation.common.model.EntityModel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Dirt", namespace = "http://www.mechaverse.org/simulation/ant/api/model")
public class Dirt extends EntityModel<EntityType> implements Serializable {

  private final static long serialVersionUID = -1L;

  public Dirt() {}

  @Override
  public EntityType getType() {
    return EntityType.DIRT;
  }
}
