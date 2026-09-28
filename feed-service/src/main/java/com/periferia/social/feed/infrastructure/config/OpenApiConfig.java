package com.periferia.social.feed.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class OpenApiConfig {

    @Bean
    OpenAPI feedOpenApi(@Value("${app.openapi.server-url:/}") String serverUrl) {
        return new OpenAPI()
                .info(new Info().title("Feed Service API").version("v1").description(
                        "Consultas reactivas del feed (CQRS read side). Tiempo real: WebSocket `/ws/feed?access_token=<JWT>` "
                                + "o SSE `/api/v1/feed/stream`."))
                .addServersItem(new Server().url(serverUrl))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
