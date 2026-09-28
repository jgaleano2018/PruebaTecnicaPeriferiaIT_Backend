package com.periferia.social.feed.domain.port.in;

import com.periferia.social.feed.domain.model.FeedPage;
import java.util.UUID;
import reactor.core.publisher.Mono;

/** Consulta: publicaciones de OTROS usuarios, más recientes primero. */
public interface GetFeedUseCase {

    int DEFAULT_PAGE_SIZE = 20;
    int MAX_PAGE_SIZE = 50;

    Mono<FeedPage> getFeed(FeedQuery query);

    record FeedQuery(UUID viewerId, String cursor, int size) {
    }
}
