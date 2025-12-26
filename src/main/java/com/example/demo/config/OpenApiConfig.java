package com.example.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API Information
                .info(new Info()
                        .title("Menu Profitability Calculator API")
                        .version("1.0")
                        .description("API for managing menu items and calculating profitability"))

                // Server configuration
                .servers(List.of(
                        new Server().url("https://9169.32procr.amypo.ai/swagger-ui/index.html")
                ))

                // Security requirement
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))

                // Security scheme definition
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
