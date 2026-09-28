package com.periferia.social.auth.infrastructure.adapter.in.seed;

import com.periferia.social.auth.domain.port.in.RegisterUserUseCase;
import com.periferia.social.auth.domain.port.in.RegisterUserUseCase.RegisterUserCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Seeder idempotente de usuarios de prueba. Cada alta emite {@code UserRegistered(seeded=true)}
 * vía Outbox; post-service lo consume y crea la publicación inicial de cada usuario.
 * Re-ejecutarlo no duplica datos.
 */
@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
class UserDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserDataSeeder.class);

    private final RegisterUserUseCase registerUser;
    private final SeedProperties properties;

    UserDataSeeder(RegisterUserUseCase registerUser, SeedProperties properties) {
        this.registerUser = registerUser;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.defaultPassword() == null || properties.defaultPassword().isBlank()) {
            log.warn("Seeder habilitado pero SEED_DEFAULT_PASSWORD está vacío; no se crearán usuarios");
            return;
        }
        int created = 0;
        for (SeedProperties.SeedUser seedUser : properties.users()) {
            if (registerUser.exists(seedUser.username())) {
                continue;
            }
            registerUser.register(new RegisterUserCommand(
                    seedUser.username(), seedUser.displayName(), properties.defaultPassword(), true));
            created++;
        }
        log.info("Seeder de usuarios: {} creado(s), {} configurado(s)", created, properties.users().size());
    }
}
