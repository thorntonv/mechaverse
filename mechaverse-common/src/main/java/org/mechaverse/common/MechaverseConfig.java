package org.mechaverse.common;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.mechaverse.service.manager.api.MechaverseManager;
import org.mechaverse.service.storage.api.MechaverseStorageService;
import org.mechaverse.simulation.api.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.google.common.collect.ImmutableList;

/**
 * Common configuration.
 */
@Configuration
@PropertySources(value = {@PropertySource("classpath:/mechaverse.properties")})
public class MechaverseConfig {

  @Value("${org.mechaverse.managerUrl}")
  private String managerUrl;

  @Value("${org.mechaverse.storageServiceUrl}")
  private String storageServiceUrl;

  @Autowired(required = false) SimulationService simulationService;

  public SimulationService getSimulationService() {
    return simulationService;
  }

  @Bean
  public MechaverseManager getManager() {
    return JAXRSClientFactory.create(managerUrl, MechaverseManager.class,
        ImmutableList.of(new JacksonJaxbJsonProvider()));
  }

  @Bean
  public MechaverseStorageService getStorageService() {
    return JAXRSClientFactory.create(storageServiceUrl, MechaverseStorageService.class,
        ImmutableList.of(new JacksonJaxbJsonProvider()));
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
     return new PropertySourcesPlaceholderConfigurer();
  }
}
