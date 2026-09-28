package com.periferia.social.post.domain.port.in;

import com.periferia.social.post.domain.model.Author;
import com.periferia.social.post.domain.model.Post;

/** Caso de uso: crear una publicación de forma idempotente. */
public interface CreatePostUseCase {

    CreatePostResult create(CreatePostCommand command);

    /** @param idempotencyKey opcional; si se envía, reintentos con la misma clave no duplican. */
    record CreatePostCommand(Author author, String message, String idempotencyKey) {
    }

    /** @param replayed {@code true} si se devolvió el resultado de una petición previa con la misma clave. */
    record CreatePostResult(Post post, boolean replayed) {
    }
}
