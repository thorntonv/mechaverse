package org.mechaverse.manager;

import org.mechaverse.manager.spring.MechaverseManagerServiceConfig;
import org.mechaverse.manager.spring.SwaggerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@ComponentScan
@Import({MechaverseManagerServiceConfig.class, SwaggerConfig.class})
@EnableAutoConfiguration
public class ManagerApp extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ManagerApp.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ManagerApp.class, args);
    }
}