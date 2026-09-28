package com.periferia.social.feed.infrastructure.adapter.in.web.dto;

import com.periferia.social.feed.domain.model.FeedPage;
import java.util.List;

public record FeedPageResponse(List<FeedItemResponse> items, String nextCursor, boolean hasMore) {

    public static FeedPageResponse from(FeedPage page) {
        return new FeedPageResponse(page.items().stream().map(FeedItemResponse::from).toList(),
                page.nextCursor(), page.hasMore());
    }
}
