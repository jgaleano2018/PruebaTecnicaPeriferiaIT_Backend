package com.periferia.social.post.infrastructure.adapter.out.persistence;

import com.periferia.social.post.domain.exception.DuplicateIdempotencyKeyException;
import com.periferia.social.post.domain.model.IdempotencyRecord;
import com.periferia.social.post.domain.port.out.IdempotencyStore;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
class IdempotencyStoreAdapter implements IdempotencyStore {

    private final IdempotencyKeyJpaRepository repository;

    IdempotencyStoreAdapter(IdempotencyKeyJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<IdempotencyRecord> find(UUID userId, String key) {
        return repository.findByUserIdAndIdempotencyKey(userId, key)
                .map(e -> new IdempotencyRecord(e.getUserId(), e.getIdempotencyKey(), e.getRequestHash(),
                        e.getPostId(), e.getCreatedAt()));
    }

    @Override
    public void save(IdempotencyRecord record) {
        try {
            // flush inmediato: la violación de unicidad debe detectarse dentro del caso de uso
            repository.saveAndFlush(new IdempotencyKeyJpaEntity(record.userId(), record.key(),
                    record.requestHash(), record.postId(), record.createdAt()));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateIdempotencyKeyException(record.key(), e);
        }
    }
}
