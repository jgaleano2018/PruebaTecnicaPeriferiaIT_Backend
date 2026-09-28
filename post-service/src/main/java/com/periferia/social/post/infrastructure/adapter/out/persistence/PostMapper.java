package com.periferia.social.post.infrastructure.adapter.out.persistence;

import com.periferia.social.post.domain.model.Author;
import com.periferia.social.post.domain.model.Post;
import com.periferia.social.post.domain.model.PostMessage;

/** Traduce entre el modelo de dominio y el modelo de persistencia (anti-corruption). */
final class PostMapper {

    private PostMapper() {
    }

    static PostJpaEntity toEntity(Post post) {
        return new PostJpaEntity(post.id(), post.author().id(), post.author().username(),
                post.author().displayName(), post.message().value(), post.publishedAt());
    }

    static Post toDomain(PostJpaEntity entity) {
        return new Post(entity.getId(),
                new Author(entity.getAuthorId(), entity.getAuthorUsername(), entity.getAuthorDisplayName()),
                new PostMessage(entity.getMessage()),
                entity.getPublishedAt());
    }
}
