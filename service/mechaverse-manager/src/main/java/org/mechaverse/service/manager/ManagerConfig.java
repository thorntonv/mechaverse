package org.mechaverse.service.manager;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Mechaverse manager configuration.
 */
@Configuration
@PropertySource("classpath:manager.properties")
public class ManagerConfig {

  @Value("${org.mechaverse.service.manager.storageServiceUrl}")
  private String storageServiceUrl;

  @Bean
  public MechaverseStorageService getStorageService() {
    return JAXRSClientFactory.create(storageServiceUrl, MechaverseStorageService.class);
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }
}
