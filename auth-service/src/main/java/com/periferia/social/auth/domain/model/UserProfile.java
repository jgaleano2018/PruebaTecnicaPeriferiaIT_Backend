package com.periferia.social.auth.domain.model;

import java.util.UUID;

/** Vista pública del usuario (sin credenciales). */
public record UserProfile(UUID id, String username, String displayName) {

    public static UserProfile of(User user) {
        return new UserProfile(user.id(), user.username(), user.displayName());
    }
}
