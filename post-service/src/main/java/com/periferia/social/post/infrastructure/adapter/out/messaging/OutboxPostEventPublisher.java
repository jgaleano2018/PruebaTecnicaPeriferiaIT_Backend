package com.periferia.social.post.infrastructure.adapter.out.messaging;

import com.periferia.social.platform.outbox.OutboxWriter;
import com.periferia.social.post.domain.model.Post;
import com.periferia.social.post.domain.port.out.PostEventPublisher;
import com.periferia.social.shared.event.PostCreatedEvent;
import java.time.Clock;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Publica {@link PostCreatedEvent} vía Transactional Outbox (consistencia con la BD). */
@Component
class OutboxPostEventPublisher implements PostEventPublisher {

    private final OutboxWriter outboxWriter;
    private final Clock clock;
    private final String topic;

    OutboxPostEventPublisher(OutboxWriter outboxWriter, Clock clock,
                             @Value("${app.kafka.topics.post-created}") String topic) {
        this.outboxWriter = outboxWriter;
        this.clock = clock;
        this.topic = topic;
    }

    @Override
    public void postCreated(Post post) {
        outboxWriter.append(new PostCreatedEvent(
                UUID.randomUUID(),
                clock.instant(),
                post.id(),
                post.author().id(),
                post.author().username(),
                post.author().displayName(),
                post.message().value(),
                post.publishedAt()), topic);
    }
}
