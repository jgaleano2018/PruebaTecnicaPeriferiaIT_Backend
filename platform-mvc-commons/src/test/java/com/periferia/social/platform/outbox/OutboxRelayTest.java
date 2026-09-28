package com.periferia.social.platform.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;

class OutboxRelayTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private OutboxEventJpaRepository repository;
    private ResilientKafkaEventPublisher publisher;
    private OutboxRelay relay;

    @BeforeEach
    void setUp() {
        repository = mock(OutboxEventJpaRepository.class);
        publisher = mock(ResilientKafkaEventPublisher.class);
        relay = new OutboxRelay(repository, publisher, mock(TransactionTemplate.class),
                Clock.fixed(NOW, ZoneOffset.UTC), 10, Duration.ofDays(7));
    }

    @Test
    void marksEveryEventAsPublishedWhenBrokerAcceptsThem() {
        OutboxEventEntity first = event();
        OutboxEventEntity second = event();
        when(repository.lockNextPendingBatch(10)).thenReturn(List.of(first, second));

        int published = relay.publishBatch();

        assertThat(published).isEqualTo(2);
        assertThat(first.getPublishedAt()).isEqualTo(NOW);
        assertThat(second.getPublishedAt()).isEqualTo(NOW);
    }

    @Test
    void stopsTheBatchOnFirstFailureToPreserveOrdering() {
        OutboxEventEntity failing = event();
        OutboxEventEntity next = event();
        when(repository.lockNextPendingBatch(10)).thenReturn(List.of(failing, next));
        doThrow(new EventPublicationException("broker down", null)).when(publisher).publish(failing);

        int published = relay.publishBatch();

        assertThat(published).isZero();
        assertThat(failing.getPublishedAt()).isNull();
        assertThat(failing.getAttempts()).isEqualTo(1);
        assertThat(failing.getLastError()).contains("broker down");
        verify(publisher, never()).publish(next);
    }

    @Test
    void doesNothingWhenThereAreNoPendingEvents() {
        when(repository.lockNextPendingBatch(10)).thenReturn(List.of());

        assertThat(relay.publishBatch()).isZero();
        verify(publisher, never()).publish(any());
    }

    private static OutboxEventEntity event() {
        return new OutboxEventEntity(UUID.randomUUID(), "agg", "PostCreated", "topic", "{}", NOW);
    }
}
