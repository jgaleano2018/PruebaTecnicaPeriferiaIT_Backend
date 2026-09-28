package com.periferia.social.auth.infrastructure.adapter.out.messaging;

import com.periferia.social.auth.domain.model.User;
import com.periferia.social.auth.domain.port.out.UserEventPublisher;
import com.periferia.social.platform.outbox.OutboxWriter;
import com.periferia.social.shared.event.UserRegisteredEvent;
import java.time.Clock;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Publica {@link UserRegisteredEvent} a través del Transactional Outbox. */
@Component
class OutboxUserEventPublisher implements UserEventPublisher {

    private final OutboxWriter outboxWriter;
    private final Clock clock;
    private final String topic;

    OutboxUserEventPublisher(OutboxWriter outboxWriter, Clock clock,
                             @Value("${app.kafka.topics.user-registered}") String topic) {
        this.outboxWriter = outboxWriter;
        this.clock = clock;
        this.topic = topic;
    }

    @Override
    public void userRegistered(User user, boolean seeded) {
        outboxWriter.append(new UserRegisteredEvent(
                UUID.randomUUID(), clock.instant(), user.id(), user.username(), user.displayName(), seeded), topic);
    }
}
