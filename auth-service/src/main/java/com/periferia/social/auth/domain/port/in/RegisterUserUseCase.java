package com.periferia.social.auth.domain.port.in;

import com.periferia.social.auth.domain.model.UserProfile;

/** Caso de uso: registrar un usuario (lo usa el seeder de datos de prueba). */
public interface RegisterUserUseCase {

    UserProfile register(RegisterUserCommand command);

    boolean exists(String username);

    record RegisterUserCommand(String username, String displayName, String rawPassword, boolean seeded) {
        @Override
        public String toString() {
            return "RegisterUserCommand[username=" + username + ", seeded=" + seeded + "]";
        }
    }
}
