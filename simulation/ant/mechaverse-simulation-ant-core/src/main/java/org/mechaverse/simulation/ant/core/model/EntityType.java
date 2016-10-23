package org.mechaverse.simulation.ant.core.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "EntityType", namespace = "http://www.mechaverse.org/simulation/ant/api/model")
@XmlEnum
public enum EntityType {

  NONE, ANT, BARRIER, ROCK, CONDUIT, DIRT, FOOD, NEST, PHEROMONE;

  public String value() {
    return name();
  }

  public static EntityType fromValue(String v) {
    return valueOf(v);
  }

}
