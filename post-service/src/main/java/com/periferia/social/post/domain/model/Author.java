package com.periferia.social.post.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Autor de una publicación (datos desnormalizados del JWT; post-service no consulta auth-service). */
public record Author(UUID id, String username, String displayName) {

    public Author {
        Objects.requireNonNull(id, "author id");
        Objects.requireNonNull(username, "author username");
        displayName = displayName == null || displayName.isBlank() ? username : displayName;
    }
}
