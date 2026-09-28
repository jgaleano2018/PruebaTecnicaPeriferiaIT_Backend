package com.periferia.social.auth.domain.port.out;

import com.periferia.social.auth.domain.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findById(UUID id);

    boolean existsByUsername(String username);

    User save(User user);
}
