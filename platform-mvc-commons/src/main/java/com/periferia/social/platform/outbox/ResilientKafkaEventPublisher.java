package com.periferia.social.platform.outbox;

import com.periferia.social.shared.event.EventHeaders;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica un registro del outbox en Kafka protegido por:
 * <ul>
 *   <li><b>Retry</b> (backoff exponencial) para fallas transitorias;</li>
 *   <li><b>Circuit Breaker</b> para no martillar un broker caído: con el circuito abierto
 *       el relay falla rápido y los eventos quedan pendientes en la tabla hasta que se recupere.</li>
 * </ul>
 * Configuración en {@code resilience4j.retry.instances.outboxPublisher} y
 * {@code resilience4j.circuitbreaker.instances.outboxPublisher}.
 */
@Component
public class ResilientKafkaEventPublisher {

    public static final String RESILIENCE_INSTANCE = "outboxPublisher";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private final long sendTimeoutMs;

    public ResilientKafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                        RetryRegistry retryRegistry,
                                        CircuitBreakerRegistry circuitBreakerRegistry,
                                        @Value("${app.outbox.send-timeout-ms:5000}") long sendTimeoutMs) {
        this.kafkaTemplate = kafkaTemplate;
        this.retry = retryRegistry.retry(RESILIENCE_INSTANCE);
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(RESILIENCE_INSTANCE);
        this.sendTimeoutMs = sendTimeoutMs;
    }

    public void publish(OutboxEventEntity event) {
        Runnable guarded = CircuitBreaker.decorateRunnable(circuitBreaker, () -> send(event));
        Retry.decorateRunnable(retry, guarded).run();
    }

    private void send(OutboxEventEntity event) {
        ProducerRecord<String, String> record =
                new ProducerRecord<>(event.getTopic(), event.getAggregateId(), event.getPayload());
        record.headers().add(EventHeaders.EVENT_ID, event.getId().toString().getBytes(StandardCharsets.UTF_8));
        record.headers().add(EventHeaders.EVENT_TYPE, event.getEventType().getBytes(StandardCharsets.UTF_8));
        try {
            kafkaTemplate.send(record).get(sendTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventPublicationException("Publicación interrumpida del evento " + event.getId(), e);
        } catch (ExecutionException | TimeoutException e) {
            throw new EventPublicationException("No fue posible publicar el evento " + event.getId(), e);
        }
    }
}
