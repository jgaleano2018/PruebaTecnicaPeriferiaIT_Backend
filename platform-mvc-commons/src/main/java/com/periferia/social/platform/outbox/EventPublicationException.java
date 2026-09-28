package com.periferia.social.platform.outbox;

/** Falla técnica al publicar un evento en el broker. */
public class EventPublicationException extends RuntimeException {

    public EventPublicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
