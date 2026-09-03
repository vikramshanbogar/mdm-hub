package com.vikram.mdmhub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mdmHubOpenApi() {
        return new OpenAPI().info(new Info()
                .title("MDM Hub API")
                .description("Modern Spring Boot MDM Hub starter - master data, cross-references and merge/survivorship")
                .version("v0.1.0"));
    }
}
