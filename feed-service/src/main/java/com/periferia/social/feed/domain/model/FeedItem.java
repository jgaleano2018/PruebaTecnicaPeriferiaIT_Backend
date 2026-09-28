package com.periferia.social.feed.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Elemento del modelo de lectura del feed (desnormalizado para lecturas rápidas). */
public record FeedItem(UUID postId, UUID authorId, String authorUsername, String authorDisplayName,
                       String message, Instant publishedAt) {

    public FeedItem {
        Objects.requireNonNull(postId, "postId");
        Objects.requireNonNull(authorId, "authorId");
        Objects.requireNonNull(publishedAt, "publishedAt");
    }

    public boolean isAuthoredBy(UUID userId) {
        return authorId.equals(userId);
    }
}
