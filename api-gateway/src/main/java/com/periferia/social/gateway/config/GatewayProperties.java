package com.periferia.social.gateway.config;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuración del gateway, 100 % externalizada en variables de entorno.
 *
 * @param authServiceUri   {@code AUTH_SERVICE_URI}
 * @param postServiceUri   {@code POST_SERVICE_URI}
 * @param feedServiceUri   {@code FEED_SERVICE_URI}
 * @param feedWebsocketUri {@code FEED_WEBSOCKET_URI} (ws://…)
 * @param allowedOrigins   {@code CORS_ALLOWED_ORIGINS} (separados por coma; incluye orígenes de Capacitor)
 */
@Validated
@ConfigurationProperties(prefix = "app.gateway")
public record GatewayProperties(
        @NotNull URI authServiceUri,
        @NotNull URI postServiceUri,
        @NotNull URI feedServiceUri,
        @NotNull URI feedWebsocketUri,
        List<String> allowedOrigins,
        Resilience resilience) {

    public GatewayProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
        resilience = resilience == null ? new Resilience(null, null, 0, null, 0) : resilience;
    }

    /**
     * @param timeout               tiempo máximo por petición antes de considerarla fallida
     * @param openStateWait         tiempo que el circuito permanece abierto
     * @param failureRateThreshold  % de fallos que abre el circuito
     * @param retryFirstBackoff     espera antes del primer reintento (solo GET)
     * @param retries               número de reintentos (solo GET, idempotentes)
     */
    public record Resilience(Duration timeout, Duration openStateWait, int failureRateThreshold,
                             Duration retryFirstBackoff, int retries) {

        public Resilience {
            timeout = timeout == null ? Duration.ofSeconds(5) : timeout;
            openStateWait = openStateWait == null ? Duration.ofSeconds(15) : openStateWait;
            failureRateThreshold = failureRateThreshold <= 0 ? 50 : failureRateThreshold;
            retryFirstBackoff = retryFirstBackoff == null ? Duration.ofMillis(100) : retryFirstBackoff;
            retries = Math.max(retries, 0);
        }
    }
}
