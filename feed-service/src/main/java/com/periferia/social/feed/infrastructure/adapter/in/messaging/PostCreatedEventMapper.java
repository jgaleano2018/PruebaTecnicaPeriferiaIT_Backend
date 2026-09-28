package com.periferia.social.feed.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.periferia.social.feed.domain.model.FeedItem;
import com.periferia.social.shared.event.PostCreatedEvent;
import org.springframework.stereotype.Component;

@Component
class PostCreatedEventMapper {

    private final ObjectMapper objectMapper;

    PostCreatedEventMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    PostCreatedEvent read(String payload) throws JsonProcessingException {
        return objectMapper.readValue(payload, PostCreatedEvent.class);
    }

    static FeedItem toFeedItem(PostCreatedEvent event) {
        return new FeedItem(event.postId(), event.authorId(), event.authorUsername(), event.authorDisplayName(),
                event.message(), event.publishedAt());
    }
}
