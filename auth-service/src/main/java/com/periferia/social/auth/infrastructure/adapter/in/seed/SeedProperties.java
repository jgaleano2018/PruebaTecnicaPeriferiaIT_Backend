package com.periferia.social.auth.infrastructure.adapter.in.seed;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Usuarios de prueba creados al iniciar. La clave se toma de {@code SEED_DEFAULT_PASSWORD}.
 */
@ConfigurationProperties(prefix = "app.seed")
public record SeedProperties(boolean enabled, String defaultPassword, List<SeedUser> users) {

    public SeedProperties {
        users = users == null ? List.of() : List.copyOf(users);
    }

    public record SeedUser(String username, String displayName) {
    }
}
