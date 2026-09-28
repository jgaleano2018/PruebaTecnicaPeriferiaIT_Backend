package com.periferia.social.platform.outbox;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Proceso de fondo que drena la tabla outbox hacia Kafka (polling publisher).
 * <p>
 * Garantía: entrega <i>at-least-once</i>. Si el proceso cae después de publicar y antes de
 * marcar el registro, el evento se reenviará; por eso los consumidores son idempotentes
 * (tabla {@code processed_event} por {@code x-event-id}).
 * <p>
 * Ante la primera falla se detiene el lote para preservar el orden de publicación.
 */
@Component
@ConditionalOnProperty(prefix = "app.outbox", name = "relay-enabled", havingValue = "true", matchIfMissing = true)
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxEventJpaRepository repository;
    private final ResilientKafkaEventPublisher publisher;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;
    private final int batchSize;
    private final Duration retention;

    public OutboxRelay(OutboxEventJpaRepository repository,
                       ResilientKafkaEventPublisher publisher,
                       TransactionTemplate transactionTemplate,
                       Clock clock,
                       @Value("${app.outbox.batch-size:50}") int batchSize,
                       @Value("${app.outbox.retention:P7D}") Duration retention) {
        this.repository = repository;
        this.publisher = publisher;
        this.transactionTemplate = transactionTemplate;
        this.clock = clock;
        this.batchSize = batchSize;
        this.retention = retention;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:1000}",
            initialDelayString = "${app.outbox.initial-delay-ms:5000}")
    public void relayPendingEvents() {
        Integer published = transactionTemplate.execute(status -> publishBatch());
        if (published != null && published > 0) {
            log.debug("Outbox relay published {} event(s)", published);
        }
    }

    int publishBatch() {
        List<OutboxEventEntity> batch = repository.lockNextPendingBatch(batchSize);
        int published = 0;
        for (OutboxEventEntity event : batch) {
            try {
                publisher.publish(event);
                event.markPublished(clock.instant());
                published++;
            } catch (RuntimeException ex) {
                event.markFailed(ex.getMessage());
                log.warn("Outbox event {} ({}) not published, will retry later: {}",
                        event.getId(), event.getEventType(), ex.getMessage());
                break;
            }
        }
        return published;
    }

    /** Limpieza periódica de eventos ya publicados para que la tabla no crezca sin límite. */
    @Scheduled(cron = "${app.outbox.cleanup-cron:0 0 3 * * *}")
    public void purgePublishedEvents() {
        Integer deleted = transactionTemplate.execute(
                status -> repository.deletePublishedBefore(clock.instant().minus(retention)));
        log.info("Outbox cleanup removed {} published event(s)", deleted);
    }
}
