package com.periferia.social.post.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.periferia.social.post.domain.model.Author;
import com.periferia.social.post.domain.model.Post;
import com.periferia.social.post.domain.port.out.PostEventPublisher;
import com.periferia.social.post.domain.port.out.PostRepository;
import com.periferia.social.post.domain.port.out.ProcessedEventStore;
import com.periferia.social.post.domain.port.out.UnitOfWork;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WelcomePostServiceTest {

    private static final Author BOB = new Author(UUID.randomUUID(), "bob", "Bob");

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostEventPublisher eventPublisher;
    @Mock
    private ProcessedEventStore processedEvents;

    private WelcomePostService service;

    @BeforeEach
    void setUp() {
        UnitOfWork direct = new UnitOfWork() {
            @Override
            public <T> T inTransaction(Supplier<T> work) {
                return work.get();
            }
        };
        service = new WelcomePostService(postRepository, eventPublisher, processedEvents, direct,
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC), "Hola, soy %s");
    }

    @Test
    void createsWelcomePostOnFirstDelivery() {
        UUID eventId = UUID.randomUUID();
        when(processedEvents.markProcessed(eq(eventId), any())).thenReturn(true);
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean created = service.handle(eventId, BOB);

        ArgumentCaptor<Post> post = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(post.capture());
        assertThat(created).isTrue();
        assertThat(post.getValue().message().value()).isEqualTo("Hola, soy Bob");
        verify(eventPublisher).postCreated(post.getValue());
    }

    @Test
    void ignoresDuplicatedDelivery() {
        UUID eventId = UUID.randomUUID();
        when(processedEvents.markProcessed(eq(eventId), any())).thenReturn(false);

        assertThat(service.handle(eventId, BOB)).isFalse();
        verify(postRepository, never()).save(any());
        verify(eventPublisher, never()).postCreated(any());
    }
}
