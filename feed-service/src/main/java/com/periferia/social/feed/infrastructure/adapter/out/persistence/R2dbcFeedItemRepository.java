package com.periferia.social.feed.infrastructure.adapter.out.persistence;

import com.periferia.social.feed.domain.model.FeedCursor;
import com.periferia.social.feed.domain.model.FeedItem;
import com.periferia.social.feed.domain.port.out.FeedItemRepository;
import io.r2dbc.spi.Readable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Adaptador R2DBC (no bloqueante) del modelo de lectura del feed. */
@Component
class R2dbcFeedItemRepository implements FeedItemRepository {

    private static final String COLUMNS =
            "post_id, author_id, author_username, author_display_name, message, published_at";

    private static final String FIRST_PAGE = """
            SELECT %s FROM feed_item
            WHERE author_id <> :viewer
            ORDER BY published_at DESC, post_id DESC
            LIMIT :limit
            """.formatted(COLUMNS);

    private static final String NEXT_PAGE = """
            SELECT %s FROM feed_item
            WHERE author_id <> :viewer
              AND (published_at, post_id) < (:cursorPublishedAt, :cursorPostId)
            ORDER BY published_at DESC, post_id DESC
            LIMIT :limit
            """.formatted(COLUMNS);

    private static final String INSERT = """
            INSERT INTO feed_item (%s)
            VALUES (:postId, :authorId, :authorUsername, :authorDisplayName, :message, :publishedAt)
            ON CONFLICT (post_id) DO NOTHING
            """.formatted(COLUMNS);

    private final DatabaseClient client;

    R2dbcFeedItemRepository(DatabaseClient client) {
        this.client = client;
    }

    @Override
    public Flux<FeedItem> findExcludingAuthor(UUID excludedAuthorId, FeedCursor cursor, int limit) {
        DatabaseClient.GenericExecuteSpec spec = cursor == null
                ? client.sql(FIRST_PAGE)
                : client.sql(NEXT_PAGE)
                        .bind("cursorPublishedAt", toOffset(cursor.publishedAt()))
                        .bind("cursorPostId", cursor.postId());
        return spec.bind("viewer", excludedAuthorId)
                .bind("limit", limit)
                .map(R2dbcFeedItemRepository::toItem)
                .all();
    }

    @Override
    public Mono<Boolean> insertIfAbsent(FeedItem item) {
        return client.sql(INSERT)
                .bind("postId", item.postId())
                .bind("authorId", item.authorId())
                .bind("authorUsername", item.authorUsername())
                .bind("authorDisplayName", item.authorDisplayName())
                .bind("message", item.message())
                .bind("publishedAt", toOffset(item.publishedAt()))
                .fetch()
                .rowsUpdated()
                .map(rows -> rows > 0);
    }

    private static FeedItem toItem(Readable row) {
        OffsetDateTime publishedAt = row.get("published_at", OffsetDateTime.class);
        return new FeedItem(
                row.get("post_id", UUID.class),
                row.get("author_id", UUID.class),
                row.get("author_username", String.class),
                row.get("author_display_name", String.class),
                row.get("message", String.class),
                publishedAt == null ? null : publishedAt.toInstant());
    }

    private static OffsetDateTime toOffset(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
