package com.periferia.social.post.domain.port.in;

import com.periferia.social.post.domain.model.Post;
import java.util.UUID;

public interface GetPostUseCase {

    Post getById(UUID postId);
}
