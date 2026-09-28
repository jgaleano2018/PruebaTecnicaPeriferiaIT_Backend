package com.periferia.social.auth.infrastructure.config;

import com.periferia.social.auth.application.service.AuthenticationService;
import com.periferia.social.auth.application.service.UserProfileService;
import com.periferia.social.auth.application.service.UserRegistrationService;
import com.periferia.social.auth.domain.port.in.AuthenticateUserUseCase;
import com.periferia.social.auth.domain.port.in.GetUserProfileUseCase;
import com.periferia.social.auth.domain.port.in.RegisterUserUseCase;
import com.periferia.social.auth.domain.port.out.AccessTokenIssuer;
import com.periferia.social.auth.domain.port.out.PasswordHasher;
import com.periferia.social.auth.domain.port.out.UnitOfWork;
import com.periferia.social.auth.domain.port.out.UserEventPublisher;
import com.periferia.social.auth.domain.port.out.UserRepository;
import com.periferia.social.platform.UnitOfWorkAdapter;
import java.time.Clock;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root: conecta los casos de uso (framework-agnósticos) con sus adaptadores.
 * Aplica inversión de dependencias (D de SOLID): la aplicación depende de puertos, no de Spring.
 */
@Configuration(proxyBeanMethods = false)
class UseCaseConfig {

    @Bean
    UnitOfWork unitOfWork(UnitOfWorkAdapter adapter) {
        return new UnitOfWork() {
            @Override
            public <T> T inTransaction(Supplier<T> work) {
                return adapter.inTransaction(work);
            }
        };
    }

    @Bean
    AuthenticateUserUseCase authenticateUserUseCase(UserRepository users, PasswordHasher hasher,
                                                    AccessTokenIssuer tokenIssuer) {
        return new AuthenticationService(users, hasher, tokenIssuer);
    }

    @Bean
    GetUserProfileUseCase getUserProfileUseCase(UserRepository users) {
        return new UserProfileService(users);
    }

    @Bean
    RegisterUserUseCase registerUserUseCase(UserRepository users, PasswordHasher hasher,
                                            UserEventPublisher events, UnitOfWork unitOfWork, Clock clock) {
        return new UserRegistrationService(users, hasher, events, unitOfWork, clock);
    }
}
