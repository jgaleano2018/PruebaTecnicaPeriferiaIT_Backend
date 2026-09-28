package com.periferia.social.post.infrastructure.adapter.out.persistence;

import com.periferia.social.post.domain.port.out.ProcessedEventStore;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class ProcessedEventStoreAdapter implements ProcessedEventStore {

    private final ProcessedEventJpaRepository repository;

    ProcessedEventStoreAdapter(ProcessedEventJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean markProcessed(UUID eventId, String eventType) {
        return repository.insertIfAbsent(eventId, eventType) == 1;
    }
}
