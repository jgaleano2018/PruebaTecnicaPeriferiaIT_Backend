package com.periferia.social.shared.event;

import java.time.Instant;
import java.util.UUID;

/** Publicado por post-service (vía Outbox) cuando se crea una publicación. */
public record PostCreatedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID postId,
        UUID authorId,
        String authorUsername,
        String authorDisplayName,
        String message,
        Instant publishedAt) implements IntegrationEvent {

    public static final String TYPE = "PostCreated";

    @Override
    public String aggregateId() {
        return authorId.toString();
    }

    @Override
    public String eventType() {
        return TYPE;
    }
}
