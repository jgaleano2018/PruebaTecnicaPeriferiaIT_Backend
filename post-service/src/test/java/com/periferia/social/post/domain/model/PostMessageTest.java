package com.periferia.social.post.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.periferia.social.shared.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PostMessageTest {

    @Test
    void stripsSurroundingWhitespace() {
        assertThat(new PostMessage("  hola  ").value()).isEqualTo("hola");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void rejectsBlankMessages(String value) {
        assertThatThrownBy(() -> new PostMessage(value)).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void rejectsMessagesLongerThanLimit() {
        assertThatThrownBy(() -> new PostMessage("a".repeat(PostMessage.MAX_LENGTH + 1)))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThat(new PostMessage("a".repeat(PostMessage.MAX_LENGTH)).value()).hasSize(PostMessage.MAX_LENGTH);
    }
}
