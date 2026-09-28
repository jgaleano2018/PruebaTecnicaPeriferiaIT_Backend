package com.periferia.social.shared.event;

import java.time.Instant;
import java.util.UUID;

/** Contrato mínimo de un evento de integración publicado entre microservicios. */
public interface IntegrationEvent {

    UUID eventId();

    Instant occurredAt();

    /** Id del agregado; se usa como clave de partición en Kafka para conservar el orden. */
    String aggregateId();

    String eventType();
}
