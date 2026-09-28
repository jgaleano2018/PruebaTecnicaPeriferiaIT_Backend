package com.periferia.social.feed.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.periferia.social.feed.domain.port.in.StreamFeedUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor de DIFUSIÓN (un grupo único por instancia, sufijo aleatorio): TODAS las réplicas
 * reciben cada evento y lo empujan a sus propios clientes WebSocket/SSE. Así el tiempo real
 * escala horizontalmente sin sesiones pegajosas ni un bus adicional.
 */
@Component
class PostCreatedBroadcastListener {

    private final StreamFeedUseCase streamFeed;
    private final PostCreatedEventMapper mapper;

    PostCreatedBroadcastListener(StreamFeedUseCase streamFeed, PostCreatedEventMapper mapper) {
        this.streamFeed = streamFeed;
        this.mapper = mapper;
    }

    @KafkaListener(id = "feed-broadcast",
            topics = "${app.kafka.topics.post-created}",
            groupId = "${app.kafka.broadcast-group-prefix}-${random.uuid}",
            properties = {"auto.offset.reset=latest"})
    void onPostCreated(String payload) throws JsonProcessingException {
        streamFeed.broadcast(PostCreatedEventMapper.toFeedItem(mapper.read(payload)));
    }
}
