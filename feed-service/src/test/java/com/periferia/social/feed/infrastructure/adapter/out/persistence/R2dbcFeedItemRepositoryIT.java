package com.periferia.social.feed.infrastructure.adapter.out.persistence;

import com.periferia.social.feed.domain.model.FeedCursor;
import com.periferia.social.feed.domain.model.FeedItem;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

/** Integra el adaptador R2DBC con PostgreSQL real (Testcontainers) y las migraciones Flyway. */
@DataR2dbcTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
@Import({R2dbcFeedItemRepository.class, R2dbcProcessedEventRepository.class})
class R2dbcFeedItemRepositoryIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://%s:%d/%s".formatted(
                POSTGRES.getHost(), POSTGRES.getMappedPort(5432), POSTGRES.getDatabaseName()));
        registry.add("spring.r2dbc.username", POSTGRES::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES::getPassword);
        registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES::getUsername);
        registry.add("spring.flyway.password", POSTGRES::getPassword);
    }

    private static final UUID VIEWER = UUID.randomUUID();
    private static final UUID OTHER = UUID.randomUUID();
    private static final Instant T0 = Instant.parse("2026-01-01T10:00:00Z");

    @Autowired
    private R2dbcFeedItemRepository repository;
    @Autowired
    private R2dbcProcessedEventRepository processedEvents;
    @Autowired
    private DatabaseClient client;

    @BeforeEach
    void cleanUp() {
        client.sql("TRUNCATE feed_item, processed_event").then().block();
    }

    @Test
    void insertIsIdempotentPerPost() {
        FeedItem item = item(OTHER, T0);

        StepVerifier.create(repository.insertIfAbsent(item)).expectNext(true).verifyComplete();
        StepVerifier.create(repository.insertIfAbsent(item)).expectNext(false).verifyComplete();
    }

    @Test
    void pagesNewestFirstExcludingViewerPosts() {
        FeedItem newest = item(OTHER, T0.plusSeconds(30));
        FeedItem middle = item(OTHER, T0.plusSeconds(20));
        FeedItem own = item(VIEWER, T0.plusSeconds(25));
        FeedItem oldest = item(OTHER, T0.plusSeconds(10));
        for (FeedItem i : new FeedItem[] {newest, middle, own, oldest}) {
            repository.insertIfAbsent(i).block();
        }

        StepVerifier.create(repository.findExcludingAuthor(VIEWER, null, 2))
                .expectNext(newest, middle)
                .verifyComplete();
        StepVerifier.create(repository.findExcludingAuthor(VIEWER, FeedCursor.of(middle), 2))
                .expectNext(oldest)
                .verifyComplete();
    }

    @Test
    void processedEventsAreRegisteredOnce() {
        UUID eventId = UUID.randomUUID();

        StepVerifier.create(processedEvents.markProcessed(eventId, "PostCreated")).expectNext(true).verifyComplete();
        StepVerifier.create(processedEvents.markProcessed(eventId, "PostCreated")).expectNext(false).verifyComplete();
    }

    private static FeedItem item(UUID author, Instant publishedAt) {
        return new FeedItem(UUID.randomUUID(), author, "user", "User", "hola", publishedAt);
    }
}
