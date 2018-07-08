package org.mechaverse.simulation.primordial.core.model;

import org.mechaverse.simulation.common.model.Entity;

import javax.xml.bind.annotation.*;
import java.io.Serializable;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PrimordialEntity", namespace = "http://www.mechaverse.org/simulation/primordial/api/model")
@XmlRootElement(name = "PrimordialEntity", namespace = "http://www.mechaverse.org/simulation/primordial/api/model")
public class PrimordialEntity extends Entity implements Serializable {

  private final static long serialVersionUID = -1L;

  public PrimordialEntity() {}

}
