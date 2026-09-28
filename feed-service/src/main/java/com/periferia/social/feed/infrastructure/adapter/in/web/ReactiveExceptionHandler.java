package com.periferia.social.feed.infrastructure.adapter.in.web;

import com.periferia.social.platform.web.ProblemDetailFactory;
import com.periferia.social.shared.exception.DomainException;
import com.periferia.social.shared.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Equivalente WebFlux del manejador global: respuestas RFC 7807 homogéneas con los servicios MVC. */
@RestControllerAdvice
class ReactiveExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ReactiveExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    Mono<ResponseEntity<ProblemDetail>> handleDomain(DomainException ex, ServerWebExchange exchange) {
        ErrorCode code = ex.getErrorCode();
        return Mono.just(build(HttpStatus.valueOf(code.httpStatus()), code, ex.getMessage(), exchange));
    }

    @ExceptionHandler(Exception.class)
    Mono<ResponseEntity<ProblemDetail>> handleUnexpected(Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error on {}", exchange.getRequest().getPath(), ex);
        return Mono.just(build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR,
                "Ocurrió un error inesperado. Intente de nuevo más tarde.", exchange));
    }

    @Override
    protected Mono<ResponseEntity<Object>> handleExceptionInternal(Exception ex, @Nullable Object body,
                                                                  @Nullable HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  ServerWebExchange exchange) {
        ProblemDetail problem = body instanceof ProblemDetail pd ? pd : ProblemDetail.forStatus(status);
        ProblemDetailFactory.enrich(problem, ErrorCode.fromHttpStatus(status.value()),
                exchange.getRequest().getPath().value());
        return super.handleExceptionInternal(ex, problem, headers, status, exchange);
    }

    private static ResponseEntity<ProblemDetail> build(HttpStatus status, ErrorCode code, String detail,
                                                       ServerWebExchange exchange) {
        return ResponseEntity.status(status)
                .body(ProblemDetailFactory.create(status, code, detail, exchange.getRequest().getPath().value()));
    }
}
