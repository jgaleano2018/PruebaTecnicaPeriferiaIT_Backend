package com.periferia.social.post.domain.port.out;

import com.periferia.social.post.domain.model.Post;

public interface PostEventPublisher {

    void postCreated(Post post);
}
