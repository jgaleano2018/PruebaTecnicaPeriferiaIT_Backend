package com.periferia.social.auth.domain.port.in;

import com.periferia.social.auth.domain.model.AuthenticationResult;

/** Caso de uso: autenticar con usuario y clave y obtener un JWT. */
public interface AuthenticateUserUseCase {

    AuthenticationResult authenticate(Credentials credentials);

    record Credentials(String username, String password) {
        @Override
        public String toString() {
            return "Credentials[username=" + username + ", password=****]";
        }
    }
}
