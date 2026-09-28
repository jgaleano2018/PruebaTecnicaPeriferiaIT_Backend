package com.periferia.social.feed.domain.port.in;

import com.periferia.social.feed.domain.model.FeedItem;
import java.util.UUID;
import reactor.core.publisher.Flux;

/** Flujo en tiempo real de publicaciones nuevas de otros usuarios. */
public interface StreamFeedUseCase {

    Flux<FeedItem> stream(UUID viewerId);

    /** Difunde una publicación nueva a los suscriptores conectados a esta instancia. */
    void broadcast(FeedItem item);
}
