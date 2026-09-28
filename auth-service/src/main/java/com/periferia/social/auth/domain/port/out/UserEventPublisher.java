package com.periferia.social.auth.domain.port.out;

import com.periferia.social.auth.domain.model.User;

/** Publica eventos de integración del agregado Usuario (implementado con Outbox). */
public interface UserEventPublisher {

    void userRegistered(User user, boolean seeded);
}
