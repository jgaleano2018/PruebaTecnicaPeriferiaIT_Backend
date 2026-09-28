package com.periferia.social.platform.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.periferia.social.shared.event.IntegrationEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Escribe un evento de integración en la tabla outbox. Exige una transacción activa
 * ({@link Propagation#MANDATORY}) para garantizar la atomicidad con el cambio de negocio.
 */
@Component
public class OutboxWriter {

    private final OutboxEventJpaRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxWriter(OutboxEventJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void append(IntegrationEvent event, String topic) {
        repository.save(new OutboxEventEntity(
                event.eventId(),
                event.aggregateId(),
                event.eventType(),
                topic,
                serialize(event),
                event.occurredAt()));
    }

    private String serialize(IntegrationEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No fue posible serializar el evento " + event.eventType(), e);
        }
    }
}
