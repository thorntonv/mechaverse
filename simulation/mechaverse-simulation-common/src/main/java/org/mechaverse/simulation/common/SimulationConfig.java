package org.mechaverse.simulation.common;

import java.io.File;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.mechaverse.circuit.model.Circuit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Common simulation configuration.
 * 
 * @author thorntonv@mechaverse.org
 */
@Configuration
@PropertySource("classpath:simulation.properties")
public class SimulationConfig {

  @Value("${org.mechaverse.simulation.circuitFilename}")
  private String circuitFilename;

  @Bean
  public Circuit circuit() throws JAXBException, URISyntaxException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Circuit.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    File XMLfile = new File(this.getClass().getClassLoader().getResource(circuitFilename).toURI());
    return (Circuit) jaxbUnmarshaller.unmarshal(XMLfile);
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
     return new PropertySourcesPlaceholderConfigurer();
  }
}
