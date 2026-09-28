package com.periferia.social.post.infrastructure.adapter.out.persistence;

import com.periferia.social.post.domain.model.Post;
import com.periferia.social.post.domain.port.out.PostRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PostPersistenceAdapter implements PostRepository {

    private final PostJpaRepository jpaRepository;

    PostPersistenceAdapter(PostJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Post save(Post post) {
        return PostMapper.toDomain(jpaRepository.save(PostMapper.toEntity(post)));
    }

    @Override
    public Optional<Post> findById(UUID id) {
        return jpaRepository.findById(id).map(PostMapper::toDomain);
    }
}
