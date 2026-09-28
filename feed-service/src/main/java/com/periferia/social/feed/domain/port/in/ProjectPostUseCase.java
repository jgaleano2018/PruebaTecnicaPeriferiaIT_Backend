package com.periferia.social.feed.domain.port.in;

import com.periferia.social.feed.domain.model.FeedItem;
import java.util.UUID;
import reactor.core.publisher.Mono;

/** Proyecta un evento PostCreated en el modelo de lectura (idempotente por eventId). */
public interface ProjectPostUseCase {

    Mono<Boolean> project(UUID eventId, FeedItem item);
}
