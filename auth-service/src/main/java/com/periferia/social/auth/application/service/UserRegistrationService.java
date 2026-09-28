package com.periferia.social.auth.application.service;

import com.periferia.social.auth.domain.model.User;
import com.periferia.social.auth.domain.model.UserProfile;
import com.periferia.social.auth.domain.port.in.RegisterUserUseCase;
import com.periferia.social.auth.domain.port.out.PasswordHasher;
import com.periferia.social.auth.domain.port.out.UnitOfWork;
import com.periferia.social.auth.domain.port.out.UserEventPublisher;
import com.periferia.social.auth.domain.port.out.UserRepository;
import com.periferia.social.shared.exception.BusinessRuleViolationException;
import com.periferia.social.shared.exception.ConflictException;
import java.time.Clock;

/**
 * Registra un usuario y, en la MISMA transacción, deja el evento {@code UserRegistered}
 * en el outbox. Si algo falla, no queda ni el usuario ni el evento.
 */
public class UserRegistrationService implements RegisterUserUseCase {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final UserEventPublisher eventPublisher;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public UserRegistrationService(UserRepository userRepository, PasswordHasher passwordHasher,
                                   UserEventPublisher eventPublisher, UnitOfWork unitOfWork, Clock clock) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.eventPublisher = eventPublisher;
        this.unitOfWork = unitOfWork;
        this.clock = clock;
    }

    @Override
    public UserProfile register(RegisterUserCommand command) {
        if (command.rawPassword() == null || command.rawPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessRuleViolationException(
                    "La clave debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres");
        }
        User user = User.register(command.username(), passwordHasher.hash(command.rawPassword()),
                command.displayName(), clock.instant());
        return unitOfWork.inTransaction(() -> {
            if (userRepository.existsByUsername(user.username())) {
                throw new ConflictException("El usuario '" + user.username() + "' ya existe");
            }
            User saved = userRepository.save(user);
            eventPublisher.userRegistered(saved, command.seeded());
            return UserProfile.of(saved);
        });
    }

    @Override
    public boolean exists(String username) {
        return userRepository.existsByUsername(User.normalizeUsername(username));
    }
}
