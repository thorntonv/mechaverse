package org.mechaverse.manager.client.spring;

import org.mechaverse.manager.ApiClient;
import org.mechaverse.manager.api.MechaverseManagerApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MechaverseManagerClientConfig {

  @Value("${managerBaseUrl}")
  private String managerBaseUrl;

  @Bean
  public MechaverseManagerApi mechaverseManagerApi() {
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(managerBaseUrl);
    return new MechaverseManagerApi(apiClient);
  }
}
