package org.mechaverse.service.storage;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySources(value = {@PropertySource("classpath:/mechaverse-storage-service.properties")})
public class StorageServiceConfig {

  @Value("${org.mechaverse.service.storage.basePath}")
  private String basePath;

  public String getBasePath() {
    return basePath;
  }

  public String getTempPath() {
    return basePath + File.separator + "temp";
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }
}
