package org.mechaverse.client;

import java.net.URI;

import org.jboss.resteasy.client.ClientRequestFactory;
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

/**
 * Common simulation configuration.
 */
@Configuration
@PropertySources(value = {@PropertySource("classpath:/mechaverse-client.properties")})
public class MechaverseClientConfig {

  @Value("${org.mechaverse.client.managerUrl}")
  private String managerUrl;

  @Value("${org.mechaverse.client.storageServiceUrl}")
  private String storageServiceUrl;

  @Autowired SimulationService simulationService;

  public SimulationService getSimulationService() {
    return simulationService;
  }

  @Bean
  public MechaverseManager getManager() {
    ClientRequestFactory clientFactory = new ClientRequestFactory(URI.create(managerUrl));
    return clientFactory.createProxy(MechaverseManager.class);
  }

  @Bean
  public MechaverseStorageService getStorageService() {
    ClientRequestFactory clientFactory = new ClientRequestFactory(URI.create(storageServiceUrl));
    return clientFactory.createProxy(MechaverseStorageService.class);
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
     return new PropertySourcesPlaceholderConfigurer();
  }
}
