package com.periferia.social.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import com.periferia.social.auth.domain.model.AccessToken;
import com.periferia.social.auth.domain.model.AuthenticationResult;
import com.periferia.social.auth.domain.model.User;
import com.periferia.social.auth.domain.port.in.AuthenticateUserUseCase.Credentials;
import com.periferia.social.auth.domain.port.out.AccessTokenIssuer;
import com.periferia.social.auth.domain.port.out.PasswordHasher;
import com.periferia.social.auth.domain.port.out.UserRepository;
import com.periferia.social.shared.exception.InvalidCredentialsException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private AccessTokenIssuer tokenIssuer;

    private AuthenticationService service;
    private User alice;

    @BeforeEach
    void setUp() {
        when(passwordHasher.hash(anyString())).thenReturn("dummy-hash");
        service = new AuthenticationService(userRepository, passwordHasher, tokenIssuer);
        alice = User.register("alice", "alice-hash", "Alice", NOW);
    }

    @Test
    void issuesTokenWhenCredentialsAreValid() {
        AccessToken token = new AccessToken("jwt", "Bearer", NOW.plusSeconds(3600));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(passwordHasher.matches("secret123", "alice-hash")).thenReturn(true);
        when(tokenIssuer.issueFor(alice)).thenReturn(token);

        AuthenticationResult result = service.authenticate(new Credentials(" ALICE ", "secret123"));

        assertThat(result.token()).isEqualTo(token);
        assertThat(result.user().username()).isEqualTo("alice");
    }

    @Test
    void rejectsWrongPasswordWithGenericMessage() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(passwordHasher.matches("bad", "alice-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.authenticate(new Credentials("alice", "bad")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage(AuthenticationService.INVALID_CREDENTIALS_MESSAGE);
        verify(tokenIssuer, never()).issueFor(any());
    }

    @Test
    void unknownUserStillComparesAgainstDummyHashToAvoidTimingLeaks() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate(new Credentials("ghost", "whatever")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage(AuthenticationService.INVALID_CREDENTIALS_MESSAGE);
        verify(passwordHasher).matches("whatever", "dummy-hash");
    }

    @Test
    void rejectsBlankCredentials() {
        assertThatThrownBy(() -> service.authenticate(new Credentials(" ", "")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(userRepository, never()).findByUsername(anyString());
    }
}
