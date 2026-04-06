package com.finance.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Finance Dashboard API")
                .description("Backend API for Finance Dashboard with Role-Based Access Control")
                .version("1.0.0")
                .contact(new Contact().name("Finance Backend")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
            .components(new Components()
                .addSecuritySchemes("Bearer Auth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Enter JWT token from /api/auth/login")));
    }
}
