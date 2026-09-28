package com.periferia.social.feed.application.service;

import com.periferia.social.feed.domain.model.FeedCursor;
import com.periferia.social.feed.domain.model.FeedItem;
import com.periferia.social.feed.domain.model.FeedPage;
import com.periferia.social.feed.domain.port.in.GetFeedUseCase;
import com.periferia.social.feed.domain.port.out.FeedItemRepository;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * Lee una página del feed pidiendo {@code size + 1} filas: si llega la fila extra, hay
 * página siguiente y el cursor apunta al último elemento devuelto.
 */
public class FeedQueryService implements GetFeedUseCase {

    private final FeedItemRepository repository;

    public FeedQueryService(FeedItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<FeedPage> getFeed(FeedQuery query) {
        int size = clamp(query.size());
        FeedCursor cursor = query.cursor() == null || query.cursor().isBlank() ? null : FeedCursor.decode(query.cursor());
        return repository.findExcludingAuthor(query.viewerId(), cursor, size + 1)
                .collectList()
                .map(rows -> toPage(rows, size));
    }

    private static FeedPage toPage(List<FeedItem> rows, int size) {
        if (rows.size() <= size) {
            return new FeedPage(rows, null);
        }
        List<FeedItem> page = rows.subList(0, size);
        return new FeedPage(page, FeedCursor.of(page.get(page.size() - 1)).encode());
    }

    private static int clamp(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
