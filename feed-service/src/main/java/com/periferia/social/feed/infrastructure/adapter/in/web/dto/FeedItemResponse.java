package com.periferia.social.feed.infrastructure.adapter.in.web.dto;

import com.periferia.social.feed.domain.model.FeedItem;
import java.time.Instant;
import java.util.UUID;

public record FeedItemResponse(UUID id, AuthorResponse author, String message, Instant publishedAt) {

    public record AuthorResponse(UUID id, String username, String displayName) {
    }

    public static FeedItemResponse from(FeedItem item) {
        return new FeedItemResponse(item.postId(),
                new AuthorResponse(item.authorId(), item.authorUsername(), item.authorDisplayName()),
                item.message(), item.publishedAt());
    }
}
