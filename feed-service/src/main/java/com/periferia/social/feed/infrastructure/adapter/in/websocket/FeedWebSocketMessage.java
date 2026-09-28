package com.periferia.social.feed.infrastructure.adapter.in.websocket;

import com.periferia.social.feed.infrastructure.adapter.in.web.dto.FeedItemResponse;

/** Mensaje enviado por el WebSocket: {@code {"type":"POST_CREATED","data":{...}}}. */
public record FeedWebSocketMessage(Type type, FeedItemResponse data) {

    public enum Type {
        CONNECTED,
        POST_CREATED,
        HEARTBEAT
    }

    public static FeedWebSocketMessage of(Type type) {
        return new FeedWebSocketMessage(type, null);
    }
}
