package org.mechaverse.manager.service.repository;

import org.mechaverse.manager.service.model.Task;
import org.springframework.data.repository.CrudRepository;

public interface TaskRepository extends CrudRepository<Task, Long> {}
