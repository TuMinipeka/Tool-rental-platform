package com.toolrental.infrastructure.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {
  public static final String BEARER_SCHEME = "bearerAuth";

  @Bean
  OpenAPI toolRentalOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Tool Rental Platform API")
                .version("0.1.0")
                .description("Academic simulation API for the Colombian tool rental marketplace."))
        .components(
            new Components()
                .addSecuritySchemes(
                    BEARER_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
  }

  @Bean
  OpenApiCustomizer onlyVersionOnePaths() {
    return openApi -> openApi.getPaths().keySet().removeIf(path -> !path.startsWith("/api/v1/"));
  }
}
