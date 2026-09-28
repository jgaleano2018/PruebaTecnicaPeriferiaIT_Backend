package com.periferia.social.auth.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.periferia.social.auth.domain.port.in.AuthenticateUserUseCase.Credentials;
import com.periferia.social.shared.exception.InvalidCredentialsException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class BasicAuthorizationParserTest {

    @Test
    void parsesUserAndPasswordAllowingColonsInPassword() {
        String header = "Basic " + Base64.getEncoder().encodeToString("alice:pa:ss".getBytes(StandardCharsets.UTF_8));

        Credentials credentials = BasicAuthorizationParser.parse(header);

        assertThat(credentials.username()).isEqualTo("alice");
        assertThat(credentials.password()).isEqualTo("pa:ss");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Bearer abc", "Basic !!!notbase64", "Basic YWxpY2U="})
    void rejectsMissingOrMalformedHeaders(String header) {
        assertThatThrownBy(() -> BasicAuthorizationParser.parse(header))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
