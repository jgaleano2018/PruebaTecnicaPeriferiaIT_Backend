package com.periferia.social.feed.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.periferia.social.feed.domain.model.FeedItem;
import com.periferia.social.feed.domain.port.out.FeedItemRepository;
import com.periferia.social.feed.domain.port.out.ProcessedEventRepository;
import com.periferia.social.feed.domain.port.out.ReactiveUnitOfWork;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class FeedProjectionServiceTest {

    @Mock
    private FeedItemRepository feedItems;
    @Mock
    private ProcessedEventRepository processedEvents;

    private FeedProjectionService service;
    private final FeedItem item =
            new FeedItem(UUID.randomUUID(), UUID.randomUUID(), "alice", "Alice", "hola", Instant.now());

    @BeforeEach
    void setUp() {
        ReactiveUnitOfWork direct = new ReactiveUnitOfWork() {
            @Override
            public <T> Mono<T> inTransaction(Mono<T> work) {
                return work;
            }
        };
        service = new FeedProjectionService(feedItems, processedEvents, direct);
    }

    @Test
    void projectsNewEvents() {
        UUID eventId = UUID.randomUUID();
        when(processedEvents.markProcessed(eq(eventId), any())).thenReturn(Mono.just(true));
        when(feedItems.insertIfAbsent(item)).thenReturn(Mono.just(true));

        StepVerifier.create(service.project(eventId, item)).expectNext(true).verifyComplete();
    }

    @Test
    void skipsAlreadyProcessedEvents() {
        UUID eventId = UUID.randomUUID();
        when(processedEvents.markProcessed(eq(eventId), any())).thenReturn(Mono.just(false));

        StepVerifier.create(service.project(eventId, item)).expectNext(false).verifyComplete();
        verify(feedItems, never()).insertIfAbsent(any());
    }
}
