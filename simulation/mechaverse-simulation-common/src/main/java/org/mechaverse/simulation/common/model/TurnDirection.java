package org.mechaverse.simulation.common.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


@XmlType(name = "TurnDirection", namespace = "http://www.mechaverse.org/simulation/common/model")
@XmlEnum
public enum TurnDirection {

  NONE, CLOCKWISE, COUNTERCLOCKWISE;

  public String value() {
    return name();
  }

  public static TurnDirection fromValue(String v) {
    return valueOf(v);
  }

}
