package com.periferia.social.feed.infrastructure.adapter.in.web;

import com.periferia.social.feed.domain.port.in.GetFeedUseCase;
import com.periferia.social.feed.domain.port.in.GetFeedUseCase.FeedQuery;
import com.periferia.social.feed.domain.port.in.StreamFeedUseCase;
import com.periferia.social.feed.infrastructure.adapter.in.web.dto.FeedItemResponse;
import com.periferia.social.feed.infrastructure.adapter.in.web.dto.FeedPageResponse;
import com.periferia.social.platform.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Duration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/feed")
@Tag(name = "Feed (consultas)", description = "Listado reactivo de publicaciones de otros usuarios")
class FeedController {

    private final GetFeedUseCase getFeed;
    private final StreamFeedUseCase streamFeed;

    FeedController(GetFeedUseCase getFeed, StreamFeedUseCase streamFeed) {
        this.getFeed = getFeed;
        this.streamFeed = streamFeed;
    }

    @GetMapping
    @Operation(summary = "Listar publicaciones de otros usuarios",
            description = "Más recientes primero. Paginación keyset: envíe `nextCursor` de la respuesta anterior.")
    Mono<FeedPageResponse> feed(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Cursor opaco devuelto por la página anterior")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Tamaño de página (1-50)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(GetFeedUseCase.MAX_PAGE_SIZE) int size) {
        return getFeed.getFeed(new FeedQuery(AuthenticatedUser.from(jwt).id(), cursor, size))
                .map(FeedPageResponse::from);
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream SSE de publicaciones nuevas",
            description = "Alternativa a WebSocket (/ws/feed) para clientes que prefieran Server-Sent Events.")
    Flux<ServerSentEvent<FeedItemResponse>> stream(@AuthenticationPrincipal Jwt jwt) {
        Flux<ServerSentEvent<FeedItemResponse>> posts = streamFeed.stream(AuthenticatedUser.from(jwt).id())
                .map(item -> ServerSentEvent.builder(FeedItemResponse.from(item)).event("post-created").build());
        Flux<ServerSentEvent<FeedItemResponse>> heartbeat = Flux.interval(Duration.ofSeconds(25))
                .map(tick -> ServerSentEvent.<FeedItemResponse>builder().comment("heartbeat").build());
        return Flux.merge(posts, heartbeat);
    }
}
