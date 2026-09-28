package com.periferia.social.post.domain.port.out;

import com.periferia.social.post.domain.model.Post;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository {

    Post save(Post post);

    Optional<Post> findById(UUID id);
}
