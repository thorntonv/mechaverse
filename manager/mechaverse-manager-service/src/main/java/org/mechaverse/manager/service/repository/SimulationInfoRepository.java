package org.mechaverse.manager.service.repository;

import org.mechaverse.manager.service.model.SimulationInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimulationInfoRepository extends CrudRepository<SimulationInfo, String> {}
