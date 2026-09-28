package com.periferia.social.gateway.fallback;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

class FallbackControllerTest {

    private final WebTestClient client = WebTestClient.bindToController(new FallbackController()).build();

    @Test
    void returnsServiceUnavailableProblemDetail() {
        client.get().uri("/fallback/feed")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.code").isEqualTo("SERVICE_UNAVAILABLE")
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.detail").value(Matchers.containsString("feed"));
    }
}
