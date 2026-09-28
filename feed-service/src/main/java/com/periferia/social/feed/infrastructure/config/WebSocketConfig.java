package com.periferia.social.feed.infrastructure.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

@Configuration(proxyBeanMethods = false)
class WebSocketConfig {

    static final String FEED_WS_PATH = "/ws/feed";

    @Bean
    HandlerMapping webSocketHandlerMapping(WebSocketHandler feedWebSocketHandler) {
        // orden alto de prioridad para que la ruta WS se resuelva antes que los controladores anotados
        return new SimpleUrlHandlerMapping(Map.of(FEED_WS_PATH, feedWebSocketHandler), -1);
    }
}
