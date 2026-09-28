package com.periferia.social.feed.infrastructure.adapter.out.notification;

import com.periferia.social.feed.domain.model.FeedItem;
import com.periferia.social.feed.domain.port.out.FeedNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Hot publisher multicast (Observer/Pub-Sub) basado en {@link Sinks}. La emisión se serializa
 * (synchronized) porque puede llegar desde varios hilos consumidores de Kafka. Cada suscriptor tiene su
 * propio buffer acotado: un cliente lento pierde los eventos más antiguos en vez de frenar a los demás.
 */
@Component
class SinkFeedNotifier implements FeedNotifier {

    private static final Logger log = LoggerFactory.getLogger(SinkFeedNotifier.class);

    private final Sinks.Many<FeedItem> sink = Sinks.many().multicast().directBestEffort();
    private final int subscriberBufferSize;

    SinkFeedNotifier(@Value("${app.realtime.subscriber-buffer-size:256}") int subscriberBufferSize) {
        this.subscriberBufferSize = subscriberBufferSize;
    }

    @Override
    public synchronized void publish(FeedItem item) {
        Sinks.EmitResult result = sink.tryEmitNext(item);
        if (result.isFailure() && result != Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER) {
            log.warn("No fue posible difundir la publicación {}: {}", item.postId(), result);
        }
    }

    @Override
    public Flux<FeedItem> updates() {
        return sink.asFlux().onBackpressureBuffer(subscriberBufferSize, dropped ->
                log.debug("Evento descartado por cliente lento: {}", dropped.postId()), BufferOverflowStrategy.DROP_OLDEST);
    }
}
