package com.periferia.social.post.application.service;

import com.periferia.social.post.domain.model.Post;
import com.periferia.social.post.domain.port.in.GetPostUseCase;
import com.periferia.social.post.domain.port.out.PostRepository;
import com.periferia.social.shared.exception.NotFoundException;
import java.util.UUID;

public class GetPostService implements GetPostUseCase {

    private final PostRepository postRepository;

    public GetPostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public Post getById(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Publicación " + postId + " no encontrada"));
    }
}
