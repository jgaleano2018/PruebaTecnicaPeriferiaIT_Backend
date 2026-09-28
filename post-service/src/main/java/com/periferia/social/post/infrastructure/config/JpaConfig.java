package com.periferia.social.post.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/** Entidades/repositorios propios + los de la plataforma (Outbox). */
@Configuration(proxyBeanMethods = false)
@EntityScan(basePackages = {"com.periferia.social.post", "com.periferia.social.platform"})
@EnableJpaRepositories(basePackages = {"com.periferia.social.post", "com.periferia.social.platform"})
class JpaConfig {
}
