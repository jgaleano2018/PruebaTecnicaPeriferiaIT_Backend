package com.periferia.social.feed.application.service;

import com.periferia.social.feed.domain.model.FeedItem;
import com.periferia.social.feed.domain.port.in.ProjectPostUseCase;
import com.periferia.social.feed.domain.port.out.FeedItemRepository;
import com.periferia.social.feed.domain.port.out.ProcessedEventRepository;
import com.periferia.social.feed.domain.port.out.ReactiveUnitOfWork;
import com.periferia.social.shared.event.PostCreatedEvent;
import java.util.UUID;
import reactor.core.publisher.Mono;

/** Idempotent Consumer reactivo: marca el evento y proyecta la fila en una sola transacción R2DBC. */
public class FeedProjectionService implements ProjectPostUseCase {

    private final FeedItemRepository feedItems;
    private final ProcessedEventRepository processedEvents;
    private final ReactiveUnitOfWork unitOfWork;

    public FeedProjectionService(FeedItemRepository feedItems, ProcessedEventRepository processedEvents,
                                 ReactiveUnitOfWork unitOfWork) {
        this.feedItems = feedItems;
        this.processedEvents = processedEvents;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public Mono<Boolean> project(UUID eventId, FeedItem item) {
        return unitOfWork.inTransaction(
                processedEvents.markProcessed(eventId, PostCreatedEvent.TYPE)
                        .flatMap(firstTime -> firstTime ? feedItems.insertIfAbsent(item) : Mono.just(false)));
    }
}
