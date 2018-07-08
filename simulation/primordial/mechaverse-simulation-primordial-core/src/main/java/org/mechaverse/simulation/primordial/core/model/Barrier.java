package org.mechaverse.simulation.primordial.core.model;

import org.mechaverse.simulation.common.model.EntityModel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Barrier", namespace = "http://www.mechaverse.org/simulation/primordial/api/model")
public class Barrier extends EntityModel implements Serializable {

  private final static long serialVersionUID = -1L;

  public Barrier() {}
}
