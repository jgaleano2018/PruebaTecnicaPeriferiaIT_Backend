package com.periferia.social.shared.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Publicado por auth-service cuando se registra un usuario.
 *
 * @param seeded {@code true} cuando el usuario fue creado por el seeder de datos de prueba.
 */
public record UserRegisteredEvent(
        UUID eventId,
        Instant occurredAt,
        UUID userId,
        String username,
        String displayName,
        boolean seeded) implements IntegrationEvent {

    public static final String TYPE = "UserRegistered";

    @Override
    public String aggregateId() {
        return userId.toString();
    }

    @Override
    public String eventType() {
        return TYPE;
    }
}
