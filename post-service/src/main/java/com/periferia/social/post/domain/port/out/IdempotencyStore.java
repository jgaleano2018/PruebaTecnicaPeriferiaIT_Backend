package com.periferia.social.post.domain.port.out;

import com.periferia.social.post.domain.model.IdempotencyRecord;
import java.util.Optional;
import java.util.UUID;

public interface IdempotencyStore {

    Optional<IdempotencyRecord> find(UUID userId, String key);

    /**
     * @throws com.periferia.social.post.domain.exception.DuplicateIdempotencyKeyException
     *         si otra transacción registró la misma clave primero.
     */
    void save(IdempotencyRecord record);
}
