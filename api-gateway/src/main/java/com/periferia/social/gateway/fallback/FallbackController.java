package com.periferia.social.gateway.fallback;

import com.periferia.social.platform.web.ProblemDetailFactory;
import com.periferia.social.shared.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Respuesta degradada cuando el Circuit Breaker de una ruta está abierto o la llamada expiró:
 * el cliente recibe un 503 RFC 7807 claro en lugar de un timeout o un error de conexión.
 */
@RestController
@RequestMapping("/fallback")
class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @RequestMapping("/{service}")
    Mono<ResponseEntity<ProblemDetail>> fallback(@PathVariable String service, ServerWebExchange exchange) {
        Throwable cause = exchange.getAttribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
        log.warn("Fallback para '{}': {}", service, cause == null ? "n/a" : cause.toString());
        ProblemDetail problem = ProblemDetailFactory.create(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.SERVICE_UNAVAILABLE,
                "El servicio '%s' no está disponible temporalmente. Intente de nuevo en unos segundos.".formatted(service),
                exchange.getRequest().getPath().value());
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem));
    }
}
