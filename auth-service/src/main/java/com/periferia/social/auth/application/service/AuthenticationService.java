package com.periferia.social.auth.application.service;

import com.periferia.social.auth.domain.model.AccessToken;
import com.periferia.social.auth.domain.model.AuthenticationResult;
import com.periferia.social.auth.domain.model.User;
import com.periferia.social.auth.domain.model.UserProfile;
import com.periferia.social.auth.domain.port.in.AuthenticateUserUseCase;
import com.periferia.social.auth.domain.port.out.AccessTokenIssuer;
import com.periferia.social.auth.domain.port.out.PasswordHasher;
import com.periferia.social.auth.domain.port.out.UserRepository;
import com.periferia.social.shared.exception.InvalidCredentialsException;
import java.util.Optional;

/**
 * Autentica usuario/clave. Devuelve siempre el mismo error genérico para no revelar si el
 * usuario existe, y compara contra un hash "señuelo" cuando no existe para igualar tiempos
 * de respuesta (mitiga enumeración de usuarios por timing).
 */
public class AuthenticationService implements AuthenticateUserUseCase {

    static final String INVALID_CREDENTIALS_MESSAGE = "Usuario o clave incorrectos";

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AccessTokenIssuer tokenIssuer;
    private final String dummyHash;

    public AuthenticationService(UserRepository userRepository, PasswordHasher passwordHasher,
                                 AccessTokenIssuer tokenIssuer) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.dummyHash = passwordHasher.hash("timing-attack-dummy-password");
    }

    @Override
    public AuthenticationResult authenticate(Credentials credentials) {
        if (credentials == null || isBlank(credentials.username()) || isBlank(credentials.password())) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }
        Optional<User> user = userRepository.findByUsername(User.normalizeUsername(credentials.username()));
        String hash = user.map(User::passwordHash).orElse(dummyHash);
        boolean matches = passwordHasher.matches(credentials.password(), hash);
        if (user.isEmpty() || !matches) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }
        AccessToken token = tokenIssuer.issueFor(user.get());
        return new AuthenticationResult(token, UserProfile.of(user.get()));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
