package com.periferia.social.feed.infrastructure.adapter.in.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.periferia.social.feed.domain.port.in.StreamFeedUseCase;
import com.periferia.social.feed.infrastructure.adapter.in.web.dto.FeedItemResponse;
import com.periferia.social.feed.infrastructure.adapter.in.websocket.FeedWebSocketMessage.Type;
import com.periferia.social.platform.security.AuthenticatedUser;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * WebSocket reactivo {@code /ws/feed?access_token=<JWT>}.
 * <p>
 * Los navegadores no permiten cabeceras personalizadas en el handshake WebSocket, por eso el JWT
 * viaja como query param y se valida aquí con el mismo {@link ReactiveJwtDecoder} de la API.
 * Se envían las publicaciones nuevas de OTROS usuarios y un heartbeat periódico para mantener
 * viva la conexión a través de proxies/balanceadores.
 */
@Component
class FeedWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(FeedWebSocketHandler.class);
    static final String TOKEN_PARAM = "access_token";

    private final ReactiveJwtDecoder jwtDecoder;
    private final StreamFeedUseCase streamFeed;
    private final ObjectMapper objectMapper;
    private final Duration heartbeatInterval;

    FeedWebSocketHandler(ReactiveJwtDecoder jwtDecoder, StreamFeedUseCase streamFeed, ObjectMapper objectMapper,
                         @Value("${app.realtime.heartbeat-interval:PT25S}") Duration heartbeatInterval) {
        this.jwtDecoder = jwtDecoder;
        this.streamFeed = streamFeed;
        this.objectMapper = objectMapper;
        this.heartbeatInterval = heartbeatInterval;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Optional<String> token = extractToken(session);
        if (token.isEmpty()) {
            return session.close(CloseStatus.POLICY_VIOLATION.withReason("Token requerido"));
        }
        return jwtDecoder.decode(token.get())
                .map(jwt -> Optional.of(AuthenticatedUser.from(jwt)))
                .onErrorResume(ex -> {
                    log.debug("WebSocket rechazado: {}", ex.getMessage());
                    return Mono.just(Optional.empty());
                })
                .flatMap(user -> user
                        .map(u -> stream(session, u))
                        .orElseGet(() -> session.close(CloseStatus.POLICY_VIOLATION.withReason("Token inválido"))));
    }

    private Mono<Void> stream(WebSocketSession session, AuthenticatedUser user) {
        Flux<FeedWebSocketMessage> posts = streamFeed.stream(user.id())
                .map(item -> new FeedWebSocketMessage(Type.POST_CREATED, FeedItemResponse.from(item)));
        Flux<FeedWebSocketMessage> heartbeat = Flux.interval(heartbeatInterval)
                .map(tick -> FeedWebSocketMessage.of(Type.HEARTBEAT));
        Flux<WebSocketMessage> outbound = Flux.concat(
                        Mono.just(FeedWebSocketMessage.of(Type.CONNECTED)),
                        Flux.merge(posts, heartbeat))
                .map(message -> session.textMessage(toJson(message)));
        // Se consume la entrada (pings/mensajes del cliente) para detectar el cierre de la sesión.
        Mono<Void> inbound = session.receive().then();
        return Mono.zip(session.send(outbound), inbound).then()
                .doFinally(signal -> log.debug("WebSocket de {} cerrado ({})", user.username(), signal));
    }

    private static Optional<String> extractToken(WebSocketSession session) {
        return Optional.ofNullable(UriComponentsBuilder.fromUri(session.getHandshakeInfo().getUri())
                        .build().getQueryParams().getFirst(TOKEN_PARAM))
                .filter(t -> !t.isBlank());
    }

    private String toJson(FeedWebSocketMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No fue posible serializar el mensaje WebSocket", e);
        }
    }
}
