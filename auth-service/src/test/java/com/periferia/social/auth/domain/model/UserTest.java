package com.periferia.social.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.periferia.social.shared.exception.BusinessRuleViolationException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void normalizesUsernameAndDefaultsDisplayName() {
        User user = User.register("  Alice ", "hash", " ", NOW);

        assertThat(user.username()).isEqualTo("alice");
        assertThat(user.displayName()).isEqualTo("alice");
        assertThat(user.id()).isNotNull();
        assertThat(user.createdAt()).isEqualTo(NOW);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "with space", "ñandú", ""})
    void rejectsInvalidUsernames(String username) {
        assertThatThrownBy(() -> User.register(username, "hash", "Name", NOW))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void rejectsTooLongDisplayName() {
        assertThatThrownBy(() -> User.register("alice", "hash", "x".repeat(101), NOW))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
