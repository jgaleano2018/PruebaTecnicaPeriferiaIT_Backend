package com.periferia.social.feed.domain.port.out;

import com.periferia.social.feed.domain.model.FeedCursor;
import com.periferia.social.feed.domain.model.FeedItem;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FeedItemRepository {

    /** @param cursor {@code null} para la primera página. */
    Flux<FeedItem> findExcludingAuthor(UUID excludedAuthorId, FeedCursor cursor, int limit);

    /** @return {@code true} si se insertó; {@code false} si la publicación ya estaba proyectada. */
    Mono<Boolean> insertIfAbsent(FeedItem item);
}
