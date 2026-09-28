package com.periferia.social.shared.event;

/**
 * Nombres lógicos de los tópicos de integración. Los nombres reales se pueden
 * sobrescribir por configuración; estas constantes son los valores por defecto.
 */
public final class KafkaTopics {

    public static final String USER_REGISTERED = "social.user.registered.v1";
    public static final String POST_CREATED = "social.post.created.v1";

    private KafkaTopics() {
    }
}
