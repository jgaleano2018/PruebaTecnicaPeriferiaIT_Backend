package com.periferia.social.shared.event;

/** Cabeceras Kafka comunes a todos los eventos de integración. */
public final class EventHeaders {

    /** Identificador único del evento; base de la idempotencia en los consumidores. */
    public static final String EVENT_ID = "x-event-id";
    /** Tipo lógico del evento (p. ej. {@code PostCreated}). */
    public static final String EVENT_TYPE = "x-event-type";

    private EventHeaders() {
    }
}
