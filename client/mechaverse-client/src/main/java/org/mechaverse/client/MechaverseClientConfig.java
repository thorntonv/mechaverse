package org.mechaverse.client;

import org.mechaverse.manager.client.spring.MechaverseManagerClientConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(MechaverseManagerClientConfig.class)
public class MechaverseClientConfig {

}
