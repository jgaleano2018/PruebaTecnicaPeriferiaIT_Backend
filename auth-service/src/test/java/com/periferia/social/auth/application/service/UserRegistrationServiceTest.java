package com.periferia.social.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.periferia.social.auth.domain.model.User;
import com.periferia.social.auth.domain.model.UserProfile;
import com.periferia.social.auth.domain.port.in.RegisterUserUseCase.RegisterUserCommand;
import com.periferia.social.auth.domain.port.out.PasswordHasher;
import com.periferia.social.auth.domain.port.out.UnitOfWork;
import com.periferia.social.auth.domain.port.out.UserEventPublisher;
import com.periferia.social.auth.domain.port.out.UserRepository;
import com.periferia.social.shared.exception.BusinessRuleViolationException;
import com.periferia.social.shared.exception.ConflictException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private UserEventPublisher eventPublisher;

    private UserRegistrationService service;

    @BeforeEach
    void setUp() {
        UnitOfWork directUnitOfWork = new UnitOfWork() {
            @Override
            public <T> T inTransaction(Supplier<T> work) {
                return work.get();
            }
        };
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        service = new UserRegistrationService(userRepository, passwordHasher, eventPublisher, directUnitOfWork, clock);
    }

    @Test
    void registersUserAndAppendsEventToOutbox() {
        when(passwordHasher.hash("Password123*")).thenReturn("hashed");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfile profile = service.register(new RegisterUserCommand("Alice", "Alice G", "Password123*", true));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().passwordHash()).isEqualTo("hashed");
        assertThat(profile.username()).isEqualTo("alice");
        verify(eventPublisher).userRegistered(saved.getValue(), true);
    }

    @Test
    void rejectsDuplicatedUsername() {
        when(passwordHasher.hash("Password123*")).thenReturn("hashed");
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterUserCommand("alice", null, "Password123*", false)))
                .isInstanceOf(ConflictException.class);
        verify(eventPublisher, never()).userRegistered(any(), anyBoolean());
    }

    @Test
    void rejectsShortPasswords() {
        assertThatThrownBy(() -> service.register(new RegisterUserCommand("alice", null, "123", false)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
