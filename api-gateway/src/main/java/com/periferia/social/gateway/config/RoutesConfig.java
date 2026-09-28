package com.periferia.social.gateway.config;

import java.net.URI;
import java.time.Duration;
import org.springframework.cloud.gateway.filter.factory.RetryGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/**
 * Tabla de enrutamiento. Cada microservicio tiene su propio Circuit Breaker (bulkhead lógico:
 * la caída de uno no degrada a los demás). El Retry solo se aplica a GET (idempotentes);
 * los POST no se reintentan en el gateway: el cliente reintenta con la misma Idempotency-Key.
 */
@Configuration(proxyBeanMethods = false)
class RoutesConfig {

    static final String AUTH_CB = "authService";
    static final String POST_CB = "postService";
    static final String FEED_CB = "feedService";

    @Bean
    RouteLocator routes(RouteLocatorBuilder builder, GatewayProperties props) {
        GatewayProperties.Resilience resilience = props.resilience();
        return builder.routes()
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .filters(f -> resilient(f, AUTH_CB, "auth", resilience))
                        .uri(props.authServiceUri()))
                .route("post-service", r -> r.path("/api/v1/posts/**")
                        .filters(f -> resilient(f, POST_CB, "posts", resilience))
                        .uri(props.postServiceUri()))
                .route("feed-service", r -> r.path("/api/v1/feed/**")
                        .filters(f -> resilient(f, FEED_CB, "feed", resilience))
                        .uri(props.feedServiceUri()))
                .route("feed-websocket", r -> r.path("/ws/**")
                        .uri(props.feedWebsocketUri()))
                // OpenAPI de cada servicio, expuesto a través del gateway para el Swagger UI agregado
                .route("auth-docs", r -> r.path("/docs/auth/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri(props.authServiceUri()))
                .route("post-docs", r -> r.path("/docs/posts/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri(props.postServiceUri()))
                .route("feed-docs", r -> r.path("/docs/feed/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri(props.feedServiceUri()))
                .build();
    }

    private static GatewayFilterSpec resilient(GatewayFilterSpec filters, String circuitBreaker, String fallback,
                                               GatewayProperties.Resilience resilience) {
        return filters
                .circuitBreaker(cb -> cb.setName(circuitBreaker)
                        .setFallbackUri(URI.create("forward:/fallback/" + fallback))
                        // respuestas de indisponibilidad del servicio también cuentan como fallo
                        .addStatusCode("502")
                        .addStatusCode("503")
                        .addStatusCode("504"))
                .retry(retry -> configureRetry(retry, resilience));
    }

    private static void configureRetry(RetryGatewayFilterFactory.RetryConfig retry,
                                       GatewayProperties.Resilience resilience) {
        retry.setRetries(resilience.retries())
                .setMethods(HttpMethod.GET)
                .setStatuses(HttpStatus.BAD_GATEWAY, HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.GATEWAY_TIMEOUT)
                .setBackoff(resilience.retryFirstBackoff(), Duration.ofSeconds(1), 2, true);
    }
}
