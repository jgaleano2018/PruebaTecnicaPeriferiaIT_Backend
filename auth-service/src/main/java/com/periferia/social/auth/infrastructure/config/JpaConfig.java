package com.periferia.social.auth.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Registra las entidades/repositorios propios y los de la plataforma (Outbox).
 * Está separado de la clase main para no afectar los tests de slice (@WebMvcTest).
 */
@Configuration(proxyBeanMethods = false)
@EntityScan(basePackages = {"com.periferia.social.auth", "com.periferia.social.platform"})
@EnableJpaRepositories(basePackages = {"com.periferia.social.auth", "com.periferia.social.platform"})
class JpaConfig {
}
