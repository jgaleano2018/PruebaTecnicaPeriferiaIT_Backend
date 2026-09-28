package com.periferia.social.feed.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.periferia.social.feed.domain.port.in.ProjectPostUseCase;
import com.periferia.social.shared.event.PostCreatedEvent;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor de PROYECCIÓN (grupo compartido): cada evento lo procesa una sola réplica del
 * servicio, que actualiza el modelo de lectura. El hilo del listener de Kafka no es un hilo de
 * event-loop, por lo que esperar el Mono (con timeout) es seguro y preserva el orden y el commit
 * de offsets tras persistir.
 */
@Component
class PostCreatedProjectionListener {

    private static final Logger log = LoggerFactory.getLogger(PostCreatedProjectionListener.class);

    private final ProjectPostUseCase projectPost;
    private final PostCreatedEventMapper mapper;
    private final Duration timeout;

    PostCreatedProjectionListener(ProjectPostUseCase projectPost, PostCreatedEventMapper mapper,
                                  @Value("${app.kafka.projection-timeout:PT10S}") Duration timeout) {
        this.projectPost = projectPost;
        this.mapper = mapper;
        this.timeout = timeout;
    }

    @KafkaListener(id = "feed-projection",
            topics = "${app.kafka.topics.post-created}",
            groupId = "${app.kafka.projection-group-id}")
    void onPostCreated(String payload) throws JsonProcessingException {
        PostCreatedEvent event = mapper.read(payload);
        Boolean projected = projectPost.project(event.eventId(), PostCreatedEventMapper.toFeedItem(event))
                .block(timeout);
        log.debug("PostCreated {} proyectado={}", event.postId(), projected);
    }
}
