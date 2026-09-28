package com.periferia.social.post.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Publicación: mensaje, usuario (autor) y fecha de publicación.
 * La fecha de publicación la asigna el sistema al guardar (no la envía el cliente).
 */
public record Post(UUID id, Author author, PostMessage message, Instant publishedAt) {

    public Post {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(author, "author");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(publishedAt, "publishedAt");
    }

    public static Post publish(Author author, PostMessage message, Instant now) {
        return new Post(UUID.randomUUID(), author, message, now);
    }
}
