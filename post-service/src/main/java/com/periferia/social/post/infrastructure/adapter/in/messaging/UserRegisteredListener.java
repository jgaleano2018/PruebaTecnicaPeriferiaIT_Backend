package com.periferia.social.post.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.periferia.social.post.domain.model.Author;
import com.periferia.social.post.domain.port.in.CreateWelcomePostUseCase;
import com.periferia.social.shared.event.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada (mensajería): cuando el seeder de auth-service registra un usuario de
 * prueba, se crea su publicación inicial. Así el seeding también atraviesa el flujo
 * Outbox → Kafka → consumidor idempotente, igual que en producción.
 */
@Component
class UserRegisteredListener {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredListener.class);

    private final CreateWelcomePostUseCase createWelcomePost;
    private final ObjectMapper objectMapper;
    private final boolean seedEnabled;

    UserRegisteredListener(CreateWelcomePostUseCase createWelcomePost, ObjectMapper objectMapper,
                           @Value("${app.seed.enabled:true}") boolean seedEnabled) {
        this.createWelcomePost = createWelcomePost;
        this.objectMapper = objectMapper;
        this.seedEnabled = seedEnabled;
    }

    @KafkaListener(topics = "${app.kafka.topics.user-registered}", groupId = "${spring.kafka.consumer.group-id}")
    void onUserRegistered(String payload) throws JsonProcessingException {
        UserRegisteredEvent event = objectMapper.readValue(payload, UserRegisteredEvent.class);
        if (!seedEnabled || !event.seeded()) {
            return;
        }
        boolean created = createWelcomePost.handle(event.eventId(),
                new Author(event.userId(), event.username(), event.displayName()));
        log.info("UserRegistered {} para '{}': publicación inicial {}", event.eventId(), event.username(),
                created ? "creada" : "ya existía (evento duplicado ignorado)");
    }
}
