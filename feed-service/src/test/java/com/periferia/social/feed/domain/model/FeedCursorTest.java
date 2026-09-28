package com.periferia.social.feed.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.periferia.social.shared.exception.BusinessRuleViolationException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FeedCursorTest {

    @Test
    void encodesAndDecodesRoundTrip() {
        FeedCursor cursor = new FeedCursor(Instant.parse("2026-05-01T10:15:30.123456Z"), UUID.randomUUID());

        assertThat(FeedCursor.decode(cursor.encode())).isEqualTo(cursor);
    }

    @ParameterizedTest
    @ValueSource(strings = {"???", "bm90LWEtY3Vyc29y", ""})
    void rejectsTamperedCursors(String encoded) {
        assertThatThrownBy(() -> FeedCursor.decode(encoded)).isInstanceOf(BusinessRuleViolationException.class);
    }
}
