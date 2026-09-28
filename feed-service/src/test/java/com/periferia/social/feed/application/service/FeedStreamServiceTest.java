package com.periferia.social.feed.application.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.periferia.social.feed.domain.model.FeedItem;
import com.periferia.social.feed.domain.port.out.FeedNotifier;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class FeedStreamServiceTest {

    @Mock
    private FeedNotifier notifier;

    @InjectMocks
    private FeedStreamService service;

    @Test
    void streamsOnlyPostsFromOtherUsers() {
        UUID viewer = UUID.randomUUID();
        FeedItem own = new FeedItem(UUID.randomUUID(), viewer, "me", "Me", "mío", Instant.now());
        FeedItem other = new FeedItem(UUID.randomUUID(), UUID.randomUUID(), "bob", "Bob", "hola", Instant.now());
        when(notifier.updates()).thenReturn(Flux.just(own, other));

        StepVerifier.create(service.stream(viewer)).expectNext(other).verifyComplete();
    }

    @Test
    void broadcastDelegatesToNotifier() {
        FeedItem item = new FeedItem(UUID.randomUUID(), UUID.randomUUID(), "bob", "Bob", "hola", Instant.now());

        service.broadcast(item);

        verify(notifier).publish(item);
    }
}
