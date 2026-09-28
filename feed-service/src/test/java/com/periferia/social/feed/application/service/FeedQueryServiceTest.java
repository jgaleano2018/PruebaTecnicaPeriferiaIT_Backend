package com.periferia.social.feed.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.periferia.social.feed.domain.model.FeedCursor;
import com.periferia.social.feed.domain.model.FeedItem;
import com.periferia.social.feed.domain.port.in.GetFeedUseCase;
import com.periferia.social.feed.domain.port.in.GetFeedUseCase.FeedQuery;
import com.periferia.social.feed.domain.port.out.FeedItemRepository;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class FeedQueryServiceTest {

    private static final UUID VIEWER = UUID.randomUUID();

    @Mock
    private FeedItemRepository repository;

    @InjectMocks
    private FeedQueryService service;

    @Test
    void returnsNextCursorWhenThereAreMoreRows() {
        FeedItem[] rows = IntStream.range(0, 3).mapToObj(i -> item(Instant.parse("2026-01-01T00:00:00Z").minusSeconds(i)))
                .toArray(FeedItem[]::new);
        when(repository.findExcludingAuthor(VIEWER, null, 3)).thenReturn(Flux.just(rows));

        StepVerifier.create(service.getFeed(new FeedQuery(VIEWER, null, 2)))
                .assertNext(page -> {
                    assertThat(page.items()).containsExactly(rows[0], rows[1]);
                    assertThat(page.hasMore()).isTrue();
                    assertThat(FeedCursor.decode(page.nextCursor())).isEqualTo(FeedCursor.of(rows[1]));
                })
                .verifyComplete();
    }

    @Test
    void lastPageHasNoCursor() {
        FeedItem only = item(Instant.now());
        when(repository.findExcludingAuthor(VIEWER, null, 21)).thenReturn(Flux.just(only));

        StepVerifier.create(service.getFeed(new FeedQuery(VIEWER, " ", 20)))
                .assertNext(page -> {
                    assertThat(page.items()).containsExactly(only);
                    assertThat(page.nextCursor()).isNull();
                })
                .verifyComplete();
    }

    @Test
    void clampsPageSizeAndDecodesCursor() {
        FeedCursor cursor = new FeedCursor(Instant.parse("2026-01-01T00:00:00Z"), UUID.randomUUID());
        when(repository.findExcludingAuthor(eq(VIEWER), eq(cursor), eq(GetFeedUseCase.MAX_PAGE_SIZE + 1)))
                .thenReturn(Flux.empty());

        StepVerifier.create(service.getFeed(new FeedQuery(VIEWER, cursor.encode(), 1_000)))
                .assertNext(page -> assertThat(page.items()).isEmpty())
                .verifyComplete();
        verify(repository).findExcludingAuthor(VIEWER, cursor, GetFeedUseCase.MAX_PAGE_SIZE + 1);
    }

    @Test
    void usesDefaultSizeForNonPositiveValues() {
        when(repository.findExcludingAuthor(eq(VIEWER), isNull(), eq(GetFeedUseCase.DEFAULT_PAGE_SIZE + 1)))
                .thenReturn(Flux.empty());

        StepVerifier.create(service.getFeed(new FeedQuery(VIEWER, null, 0)))
                .expectNextCount(1)
                .verifyComplete();
    }

    private static FeedItem item(Instant publishedAt) {
        return new FeedItem(UUID.randomUUID(), UUID.randomUUID(), "bob", "Bob", "hola", publishedAt);
    }
}
