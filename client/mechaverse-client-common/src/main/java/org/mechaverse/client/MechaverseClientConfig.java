package org.mechaverse.client;

import org.mechaverse.simulation.api.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Common simulation configuration.
 */
@Configuration
@PropertySource("classpath:mechaverse-client.properties")
public class MechaverseClientConfig {

  @Value("${org.mechaverse.client.managerUrl}")
  private String managerUrl;

  @Autowired SimulationService simulationService;

  public SimulationService getSimulationService() {
    return simulationService;
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
     return new PropertySourcesPlaceholderConfigurer();
  }
}
