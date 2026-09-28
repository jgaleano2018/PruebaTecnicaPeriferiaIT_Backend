package com.periferia.social.feed.domain.model;

import java.util.List;

/** Página del feed. {@code nextCursor} es {@code null} cuando no hay más resultados. */
public record FeedPage(List<FeedItem> items, String nextCursor) {

    public FeedPage {
        items = List.copyOf(items);
    }

    public boolean hasMore() {
        return nextCursor != null;
    }
}
