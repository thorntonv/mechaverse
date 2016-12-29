package org.mechaverse.simulation.primordial.core.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "EntityType", namespace = "http://www.mechaverse.org/simulation/primordial/api/model")
@XmlEnum
public enum EntityType {

  NONE, ENTITY, BARRIER, FOOD;

  public String value() {
    return name();
  }

  public static EntityType fromValue(String v) {
    return valueOf(v);
  }

}
