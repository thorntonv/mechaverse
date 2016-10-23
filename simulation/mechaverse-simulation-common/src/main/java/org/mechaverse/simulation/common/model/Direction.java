package org.mechaverse.simulation.common.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


@XmlType(name = "Direction", namespace = "http://www.mechaverse.org/simulation/api/model")
@XmlEnum
public enum Direction {

  EAST, NORTH_EAST, NORTH, NORTH_WEST, WEST, SOUTH_WEST, SOUTH, SOUTH_EAST;

  public String value() {
    return name();
  }

  public static Direction fromValue(String v) {
    return valueOf(v);
  }

}
