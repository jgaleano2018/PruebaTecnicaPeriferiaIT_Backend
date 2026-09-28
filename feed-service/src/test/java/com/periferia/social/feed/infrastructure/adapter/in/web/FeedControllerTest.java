package com.periferia.social.feed.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import com.periferia.social.feed.domain.model.FeedItem;
import com.periferia.social.feed.domain.model.FeedPage;
import com.periferia.social.feed.domain.port.in.GetFeedUseCase;
import com.periferia.social.feed.domain.port.in.StreamFeedUseCase;
import com.periferia.social.feed.infrastructure.config.SecurityConfig;
import com.periferia.social.shared.exception.BusinessRuleViolationException;
import com.periferia.social.shared.security.JwtClaimNames;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = FeedController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.security.jwt.secret=test-secret-with-at-least-32-characters!!",
        "app.security.jwt.issuer=test-issuer"
})
class FeedControllerTest {

    private static final UUID VIEWER = UUID.randomUUID();

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private GetFeedUseCase getFeed;
    @MockitoBean
    private StreamFeedUseCase streamFeed;

    @Test
    void returnsFeedOfOtherUsers() {
        FeedItem item = new FeedItem(UUID.randomUUID(), UUID.randomUUID(), "bob", "Bob", "hola", Instant.now());
        when(getFeed.getFeed(argThat(q -> q.viewerId().equals(VIEWER) && q.size() == 10)))
                .thenReturn(Mono.just(new FeedPage(List.of(item), "next")));

        client.mutateWith(viewerJwt())
                .get().uri("/api/v1/feed?size=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items[0].message").isEqualTo("hola")
                .jsonPath("$.items[0].author.username").isEqualTo("bob")
                .jsonPath("$.nextCursor").isEqualTo("next")
                .jsonPath("$.hasMore").isEqualTo(true);
    }

    @Test
    void rejectsInvalidPageSize() {
        client.mutateWith(viewerJwt())
                .get().uri("/api/v1/feed?size=0")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void invalidCursorReturns422() {
        when(getFeed.getFeed(argThat(q -> "bad".equals(q.cursor()))))
                .thenReturn(Mono.error(new BusinessRuleViolationException("Cursor de paginación inválido")));

        client.mutateWith(viewerJwt())
                .get().uri("/api/v1/feed?cursor=bad")
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody().jsonPath("$.code").isEqualTo("BUSINESS_RULE_VIOLATION");
    }

    @Test
    void requiresAuthentication() {
        client.get().uri("/api/v1/feed")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody().jsonPath("$.code").isEqualTo("UNAUTHORIZED");
    }

    private static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.JwtMutator viewerJwt() {
        return mockJwt().jwt(j -> j.subject(VIEWER.toString()).claim(JwtClaimNames.USERNAME, "alice"));
    }
}
