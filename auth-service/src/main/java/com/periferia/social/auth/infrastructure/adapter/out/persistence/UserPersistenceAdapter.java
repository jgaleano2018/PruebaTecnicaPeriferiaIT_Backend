package com.periferia.social.auth.infrastructure.adapter.out.persistence;

import com.periferia.social.auth.domain.model.User;
import com.periferia.social.auth.domain.port.out.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adaptador de salida: implementa el puerto {@link UserRepository} con JPA/Hibernate. */
@Component
class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    UserPersistenceAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(UserPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(UserPersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public User save(User user) {
        return toDomain(jpaRepository.save(new UserJpaEntity(
                user.id(), user.username(), user.passwordHash(), user.displayName(), user.createdAt())));
    }

    private static User toDomain(UserJpaEntity entity) {
        return User.restore(entity.getId(), entity.getUsername(), entity.getPasswordHash(),
                entity.getDisplayName(), entity.getCreatedAt());
    }
}
