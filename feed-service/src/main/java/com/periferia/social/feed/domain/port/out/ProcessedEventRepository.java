package com.periferia.social.feed.domain.port.out;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ProcessedEventRepository {

    Mono<Boolean> markProcessed(UUID eventId, String eventType);
}
