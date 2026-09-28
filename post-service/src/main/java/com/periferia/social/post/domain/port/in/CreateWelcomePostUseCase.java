package com.periferia.social.post.domain.port.in;

import com.periferia.social.post.domain.model.Author;
import java.util.UUID;

/**
 * Caso de uso disparado por el evento {@code UserRegistered(seeded=true)}: crea la
 * publicación inicial de cada usuario de prueba. Idempotente por {@code eventId}.
 */
public interface CreateWelcomePostUseCase {

    /** @return {@code true} si se creó la publicación; {@code false} si el evento ya se había procesado. */
    boolean handle(UUID eventId, Author author);
}
