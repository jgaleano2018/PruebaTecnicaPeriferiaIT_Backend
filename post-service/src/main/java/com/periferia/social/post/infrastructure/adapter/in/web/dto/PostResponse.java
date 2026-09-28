package com.periferia.social.post.infrastructure.adapter.in.web.dto;

import com.periferia.social.post.domain.model.Post;
import java.time.Instant;
import java.util.UUID;

public record PostResponse(UUID id, AuthorResponse author, String message, Instant publishedAt) {

    public record AuthorResponse(UUID id, String username, String displayName) {
    }

    public static PostResponse from(Post post) {
        return new PostResponse(post.id(),
                new AuthorResponse(post.author().id(), post.author().username(), post.author().displayName()),
                post.message().value(),
                post.publishedAt());
    }
}
