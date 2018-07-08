package org.mechaverse.simulation.primordial.core.model;

import org.mechaverse.simulation.common.model.EntityModel;

import javax.xml.bind.annotation.*;
import java.io.Serializable;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PrimordialEntityModel", namespace = "http://www.mechaverse.org/simulation/primordial/api/model")
@XmlRootElement(name = "PrimordialEntityModel", namespace = "http://www.mechaverse.org/simulation/primordial/api/model")
public class PrimordialEntityModel extends EntityModel implements Serializable {

  private final static long serialVersionUID = -1L;

  public PrimordialEntityModel() {}

}
