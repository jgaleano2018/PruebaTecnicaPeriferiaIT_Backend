package com.periferia.social.feed.domain.model;

import com.periferia.social.shared.exception.BusinessRuleViolationException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Cursor opaco para paginación keyset por {@code (published_at, post_id)}. A diferencia de
 * OFFSET, es estable aunque lleguen publicaciones nuevas en tiempo real y escala con el volumen.
 */
public record FeedCursor(Instant publishedAt, UUID postId) {

    private static final String SEPARATOR = "|";

    public static FeedCursor of(FeedItem item) {
        return new FeedCursor(item.publishedAt(), item.postId());
    }

    public String encode() {
        String raw = publishedAt.toString() + SEPARATOR + postId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static FeedCursor decode(String encoded) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
            int idx = raw.indexOf(SEPARATOR);
            return new FeedCursor(Instant.parse(raw.substring(0, idx)), UUID.fromString(raw.substring(idx + 1)));
        } catch (RuntimeException e) {
            throw new BusinessRuleViolationException("Cursor de paginación inválido");
        }
    }
}
