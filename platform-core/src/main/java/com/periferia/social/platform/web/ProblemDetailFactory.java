package com.periferia.social.platform.web;

import com.periferia.social.shared.exception.ErrorCode;
import java.net.URI;
import java.time.Instant;
import org.slf4j.MDC;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

/**
 * Construye respuestas de error homogéneas (RFC 7807 / 9457) con propiedades extendidas:
 * {@code code} (estable, para el cliente), {@code timestamp} y {@code traceId}
 * (para correlacionar con la traza distribuida en Jaeger).
 */
public final class ProblemDetailFactory {

    private static final String TYPE_BASE = "https://api.periferia-social.dev/errors/";

    private ProblemDetailFactory() {
    }

    public static ProblemDetail create(HttpStatusCode status, ErrorCode code, String detail, String instance) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        return enrich(problem, code, instance);
    }

    public static ProblemDetail enrich(ProblemDetail problem, ErrorCode code, String instance) {
        problem.setType(URI.create(TYPE_BASE + code.name().toLowerCase().replace('_', '-')));
        if (instance != null) {
            problem.setInstance(URI.create(instance));
        }
        problem.setProperty("code", code.name());
        problem.setProperty("timestamp", Instant.now().toString());
        String traceId = MDC.get("traceId");
        if (traceId != null) {
            problem.setProperty("traceId", traceId);
        }
        return problem;
    }
}
