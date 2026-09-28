package com.periferia.social.feed.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.periferia.social.platform.security.JwtProperties;
import com.periferia.social.platform.security.JwtSecretKeys;
import com.periferia.social.platform.web.ProblemDetailFactory;
import com.periferia.social.shared.exception.ErrorCode;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
@EnableWebFluxSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private static final String[] PUBLIC = {
            "/ws/**", // el handler WebSocket valida el JWT del query param
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**",
            "/actuator/health/**", "/actuator/info", "/actuator/prometheus"
    };

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ObjectMapper objectMapper) {
        ServerAuthenticationEntryPoint entryPoint = (exchange, ex) ->
                write(exchange, objectMapper, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED,
                        "Se requiere un token JWT válido");
        ServerAccessDeniedHandler deniedHandler = (exchange, ex) ->
                write(exchange, objectMapper, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN,
                        "No tiene permisos para esta operación");
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable) // CORS se resuelve en el API Gateway
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers(PUBLIC).permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(rs -> rs
                        .jwt(jwt -> { })
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(deniedHandler))
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(deniedHandler))
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

    static Mono<Void> write(ServerWebExchange exchange, ObjectMapper mapper, HttpStatus status, ErrorCode code,
                            String detail) {
        ProblemDetail problem = ProblemDetailFactory.create(status, code, detail,
                exchange.getRequest().getPath().value());
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return Mono.fromCallable(() -> mapper.writeValueAsBytes(problem))
                .flatMap(bytes -> {
                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                    return exchange.getResponse().writeWith(Mono.just(buffer));
                });
    }
}
