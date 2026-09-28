package com.periferia.social.post.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.periferia.social.post.domain.exception.DuplicateIdempotencyKeyException;
import com.periferia.social.post.domain.model.Author;
import com.periferia.social.post.domain.model.IdempotencyRecord;
import com.periferia.social.post.domain.model.Post;
import com.periferia.social.post.domain.model.PostMessage;
import com.periferia.social.post.domain.port.in.CreatePostUseCase.CreatePostCommand;
import com.periferia.social.post.domain.port.in.CreatePostUseCase.CreatePostResult;
import com.periferia.social.post.domain.port.out.IdempotencyStore;
import com.periferia.social.post.domain.port.out.PostEventPublisher;
import com.periferia.social.post.domain.port.out.PostRepository;
import com.periferia.social.post.domain.port.out.UnitOfWork;
import com.periferia.social.shared.exception.BusinessRuleViolationException;
import com.periferia.social.shared.exception.IdempotencyKeyReusedException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreatePostServiceTest {

    private static final Instant NOW = Instant.parse("2026-03-01T12:00:00Z");
    private static final Author ALICE = new Author(UUID.randomUUID(), "alice", "Alice");

    @Mock
    private PostRepository postRepository;
    @Mock
    private IdempotencyStore idempotencyStore;
    @Mock
    private PostEventPublisher eventPublisher;

    private CreatePostService service;

    @BeforeEach
    void setUp() {
        UnitOfWork direct = new UnitOfWork() {
            @Override
            public <T> T inTransaction(Supplier<T> work) {
                return work.get();
            }
        };
        service = new CreatePostService(postRepository, idempotencyStore, eventPublisher, direct,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsPostWithServerAssignedDateAndPublishesEvent() {
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        CreatePostResult result = service.create(new CreatePostCommand(ALICE, "  Hola mundo ", null));

        assertThat(result.replayed()).isFalse();
        assertThat(result.post().message().value()).isEqualTo("Hola mundo");
        assertThat(result.post().publishedAt()).isEqualTo(NOW);
        assertThat(result.post().author()).isEqualTo(ALICE);
        verify(eventPublisher).postCreated(result.post());
        verify(idempotencyStore, never()).save(any());
    }

    @Test
    void storesIdempotencyRecordWhenKeyIsProvided() {
        when(idempotencyStore.find(ALICE.id(), "key-1")).thenReturn(Optional.empty());
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        CreatePostResult result = service.create(new CreatePostCommand(ALICE, "Hola", "key-1"));

        ArgumentCaptor<IdempotencyRecord> record = ArgumentCaptor.forClass(IdempotencyRecord.class);
        verify(idempotencyStore).save(record.capture());
        assertThat(record.getValue().postId()).isEqualTo(result.post().id());
        assertThat(record.getValue().requestHash()).isEqualTo(CreatePostService.sha256("Hola"));
    }

    @Test
    void replaysOriginalPostForRepeatedKeyAndSamePayload() {
        Post original = Post.publish(ALICE, new PostMessage("Hola"), NOW.minusSeconds(5));
        when(idempotencyStore.find(ALICE.id(), "key-1")).thenReturn(Optional.of(
                new IdempotencyRecord(ALICE.id(), "key-1", CreatePostService.sha256("Hola"), original.id(), NOW)));
        when(postRepository.findById(original.id())).thenReturn(Optional.of(original));

        CreatePostResult result = service.create(new CreatePostCommand(ALICE, "Hola", "key-1"));

        assertThat(result.replayed()).isTrue();
        assertThat(result.post()).isEqualTo(original);
        verify(postRepository, never()).save(any());
        verify(eventPublisher, never()).postCreated(any());
    }

    @Test
    void rejectsReusedKeyWithDifferentPayload() {
        when(idempotencyStore.find(ALICE.id(), "key-1")).thenReturn(Optional.of(
                new IdempotencyRecord(ALICE.id(), "key-1", CreatePostService.sha256("Otro"), UUID.randomUUID(), NOW)));

        assertThatThrownBy(() -> service.create(new CreatePostCommand(ALICE, "Hola", "key-1")))
                .isInstanceOf(IdempotencyKeyReusedException.class);
    }

    @Test
    void concurrentDuplicateKeyReturnsTheWinningRequestResult() {
        Post winner = Post.publish(ALICE, new PostMessage("Hola"), NOW);
        IdempotencyRecord winnerRecord =
                new IdempotencyRecord(ALICE.id(), "key-1", CreatePostService.sha256("Hola"), winner.id(), NOW);
        when(idempotencyStore.find(ALICE.id(), "key-1"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(winnerRecord));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new DuplicateIdempotencyKeyException("key-1", null)).when(idempotencyStore).save(any());
        when(postRepository.findById(winner.id())).thenReturn(Optional.of(winner));

        CreatePostResult result = service.create(new CreatePostCommand(ALICE, "Hola", "key-1"));

        assertThat(result.replayed()).isTrue();
        assertThat(result.post()).isEqualTo(winner);
    }

    @Test
    void rejectsInvalidMessageBeforeTouchingPersistence() {
        assertThatThrownBy(() -> service.create(new CreatePostCommand(ALICE, "  ", null)))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(postRepository, never()).save(any());
    }

    @Test
    void rejectsOversizedIdempotencyKey() {
        assertThatThrownBy(() -> service.create(new CreatePostCommand(ALICE, "Hola", "k".repeat(101))))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
