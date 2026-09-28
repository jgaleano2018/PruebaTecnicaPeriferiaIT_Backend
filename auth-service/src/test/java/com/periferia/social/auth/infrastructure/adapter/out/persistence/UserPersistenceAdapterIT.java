package com.periferia.social.auth.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.periferia.social.auth.domain.model.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Prueba de integración del adaptador JPA contra PostgreSQL real (Testcontainers) + Flyway. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@Import(UserPersistenceAdapter.class)
class UserPersistenceAdapterIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserPersistenceAdapter adapter;

    @Test
    void savesAndFindsUsersByUsername() {
        User user = User.register("alice", "hash", "Alice", Instant.now().truncatedTo(ChronoUnit.MILLIS));

        adapter.save(user);

        assertThat(adapter.existsByUsername("alice")).isTrue();
        assertThat(adapter.findByUsername("alice"))
                .hasValueSatisfying(found -> {
                    assertThat(found.id()).isEqualTo(user.id());
                    assertThat(found.displayName()).isEqualTo("Alice");
                });
        assertThat(adapter.findByUsername("nobody")).isEmpty();
    }
}
