package com.periferia.social.platform.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentación OpenAPI/Swagger con esquema Bearer JWT, común a los servicios MVC. */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    OpenAPI serviceOpenApi(@Value("${app.openapi.title:Social Network API}") String title,
                           @Value("${app.openapi.description:}") String description,
                           @Value("${app.openapi.version:v1}") String version,
                           @Value("${app.openapi.server-url:/}") String serverUrl) {
        return new OpenAPI()
                .info(new Info().title(title).description(description).version(version))
                .addServersItem(new Server().url(serverUrl))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
