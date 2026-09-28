package com.periferia.social.post.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.periferia.social.post.domain.exception.DuplicateIdempotencyKeyException;
import com.periferia.social.post.domain.model.Author;
import com.periferia.social.post.domain.model.IdempotencyRecord;
import com.periferia.social.post.domain.model.Post;
import com.periferia.social.post.domain.model.PostMessage;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Verifica contra PostgreSQL real (Testcontainers + migraciones Flyway) las garantías que
 * dependen de la base de datos: unicidad de Idempotency-Key e inserción idempotente de eventos.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@Import({PostPersistenceAdapter.class, IdempotencyStoreAdapter.class, ProcessedEventStoreAdapter.class})
class PostPersistenceIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private PostPersistenceAdapter posts;
    @Autowired
    private IdempotencyStoreAdapter idempotencyStore;
    @Autowired
    private ProcessedEventStoreAdapter processedEvents;

    @Test
    void persistsAndReadsPosts() {
        Post post = newPost();

        posts.save(post);

        assertThat(posts.findById(post.id())).contains(post);
    }

    @Test
    void idempotencyKeyIsUniquePerUser() {
        Post post = posts.save(newPost());
        IdempotencyRecord record = new IdempotencyRecord(post.author().id(), "key-1", "hash", post.id(), post.publishedAt());
        idempotencyStore.save(record);

        assertThat(idempotencyStore.find(post.author().id(), "key-1")).isPresent();
        assertThatThrownBy(() -> idempotencyStore.save(record)).isInstanceOf(DuplicateIdempotencyKeyException.class);
    }

    @Test
    void processedEventIsRegisteredOnlyOnce() {
        UUID eventId = UUID.randomUUID();

        assertThat(processedEvents.markProcessed(eventId, "UserRegistered")).isTrue();
        assertThat(processedEvents.markProcessed(eventId, "UserRegistered")).isFalse();
    }

    private static Post newPost() {
        return Post.publish(new Author(UUID.randomUUID(), "alice", "Alice"), new PostMessage("Hola"),
                Instant.now().truncatedTo(ChronoUnit.MICROS));
    }
}
