package com.periferia.social.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.periferia.social.gateway.config.GatewayProperties;
import com.periferia.social.platform.security.JwtProperties;
import com.periferia.social.platform.security.JwtSecretKeys;
import com.periferia.social.platform.web.ProblemDetailFactory;
import com.periferia.social.shared.exception.ErrorCode;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Primera barrera de seguridad: rechaza en el borde las peticiones sin JWT válido (ahorra
 * carga a los servicios) y resuelve CORS para web y apps Capacitor (iOS/Android).
 */
@Configuration(proxyBeanMethods = false)
@EnableWebFluxSecurity
@EnableConfigurationProperties(JwtProperties.class)
class SecurityConfig {

    private static final String[] PUBLIC = {
            "/api/v1/auth/login",
            "/ws/**",
            "/fallback/**",
            "/docs/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**",
            "/actuator/health/**", "/actuator/info", "/actuator/prometheus"
    };

    @Bean
    SecurityWebFilterChain gatewaySecurity(ServerHttpSecurity http, ObjectMapper objectMapper) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers(PUBLIC).permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(rs -> rs
                        .jwt(jwt -> { })
                        .authenticationEntryPoint((exchange, ex) -> write(exchange, objectMapper,
                                HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "Se requiere un token JWT válido")))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((exchange, ex) -> write(exchange, objectMapper,
                                HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "Se requiere un token JWT válido"))
                        .accessDeniedHandler((exchange, ex) -> write(exchange, objectMapper,
                                HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, "No tiene permisos para esta operación")))
                .build();
    }

    @Bean
    ReactiveJwtDecoder reactiveJwtDecoder(JwtProperties properties) {
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(JwtSecretKeys.hmacKey(properties))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
        return decoder;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(GatewayProperties props) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(props.allowedOrigins());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT,
                "Idempotency-Key", "X-Request-Id"));
        cors.setExposedHeaders(List.of(HttpHeaders.LOCATION, "Idempotent-Replayed", "X-Request-Id"));
        cors.setAllowCredentials(false); // JWT en cabecera, no cookies
        cors.setMaxAge(Duration.ofHours(1));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    private static Mono<Void> write(ServerWebExchange exchange, ObjectMapper mapper, HttpStatus status,
                                    ErrorCode code, String detail) {
        ProblemDetail problem = ProblemDetailFactory.create(status, code, detail,
                exchange.getRequest().getPath().value());
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return Mono.fromCallable(() -> mapper.writeValueAsBytes(problem))
                .flatMap(bytes -> exchange.getResponse()
                        .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))));
    }
}
