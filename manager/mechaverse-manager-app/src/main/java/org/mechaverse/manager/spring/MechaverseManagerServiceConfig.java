package org.mechaverse.manager.spring;

import org.mechaverse.manager.service.HibernateMechaverseManagerService;
import org.mechaverse.manager.service.MechaverseManagerService;
import org.mechaverse.manager.service.storage.LocalFileMechaverseStorageService;
import org.mechaverse.manager.service.storage.MechaverseStorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MechaverseManagerServiceConfig {

  @Bean
  public MechaverseManagerService managerService() {
    return new HibernateMechaverseManagerService();
  }

  @Bean
  public MechaverseStorageService storageService() {
    return new LocalFileMechaverseStorageService();
  }
}
