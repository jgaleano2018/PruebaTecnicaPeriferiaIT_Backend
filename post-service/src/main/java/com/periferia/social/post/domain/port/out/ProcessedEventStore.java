package com.periferia.social.post.domain.port.out;

import java.util.UUID;

/** Soporte del patrón Idempotent Consumer. */
public interface ProcessedEventStore {

    /** Marca el evento como procesado. @return {@code false} si ya lo estaba. */
    boolean markProcessed(UUID eventId, String eventType);
}
