package com.periferia.social.feed.infrastructure.adapter.out.persistence;

import com.periferia.social.feed.domain.port.out.ProcessedEventRepository;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
class R2dbcProcessedEventRepository implements ProcessedEventRepository {

    private static final String INSERT = """
            INSERT INTO processed_event (event_id, event_type, processed_at)
            VALUES (:eventId, :eventType, now())
            ON CONFLICT (event_id) DO NOTHING
            """;

    private final DatabaseClient client;

    R2dbcProcessedEventRepository(DatabaseClient client) {
        this.client = client;
    }

    @Override
    public Mono<Boolean> markProcessed(UUID eventId, String eventType) {
        return client.sql(INSERT)
                .bind("eventId", eventId)
                .bind("eventType", eventType)
                .fetch()
                .rowsUpdated()
                .map(rows -> rows > 0);
    }
}
