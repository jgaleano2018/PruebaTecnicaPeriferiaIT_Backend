package com.periferia.social.post.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Registro que vincula una Idempotency-Key (por usuario) con la publicación que generó. */
public record IdempotencyRecord(UUID userId, String key, String requestHash, UUID postId, Instant createdAt) {

    public boolean matches(String otherRequestHash) {
        return requestHash.equals(otherRequestHash);
    }
}
