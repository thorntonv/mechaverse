package org.mechaverse.simulation.common.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "MoveDirection", namespace = "http://www.mechaverse.org/simulation/common/model")
@XmlEnum
public enum MoveDirection {

  NONE, FORWARD, BACKWARD;

  public String value() {
    return name();
  }

  public static MoveDirection fromValue(String v) {
    return valueOf(v);
  }

}
