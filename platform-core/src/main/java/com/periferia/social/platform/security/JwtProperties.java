package com.periferia.social.platform.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuración JWT compartida. Todos los valores provienen de variables de entorno
 * (ver {@code .env.example}); no hay secretos en el código.
 *
 * @param secret     clave HMAC-SHA256 (mínimo 32 caracteres) — {@code JWT_SECRET}
 * @param issuer     emisor esperado — {@code JWT_ISSUER}
 * @param expiration vigencia del token — {@code JWT_EXPIRATION}
 */
@Validated
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32, message = "JWT_SECRET debe tener al menos 32 caracteres") String secret,
        @NotBlank String issuer,
        Duration expiration) {

    public JwtProperties {
        if (expiration == null) {
            expiration = Duration.ofHours(1);
        }
    }
}
