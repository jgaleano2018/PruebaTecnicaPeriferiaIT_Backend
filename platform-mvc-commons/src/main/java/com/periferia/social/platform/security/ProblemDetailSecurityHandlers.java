package com.periferia.social.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.periferia.social.platform.web.ProblemDetailFactory;
import com.periferia.social.shared.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Los errores 401/403 que produce la cadena de filtros de Spring Security ocurren antes
 * de llegar a los controladores; aquí se serializan con el mismo formato RFC 7807.
 */
@Component
public class ProblemDetailSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ProblemDetailSecurityHandlers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer");
        write(response, request, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED,
                "Se requiere un token JWT válido");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       org.springframework.security.access.AccessDeniedException ex) throws IOException {
        write(response, request, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, "No tiene permisos para esta operación");
    }

    private void write(HttpServletResponse response, HttpServletRequest request, HttpStatus status,
                       ErrorCode code, String detail) throws IOException {
        ProblemDetail problem = ProblemDetailFactory.create(status, code, detail, request.getRequestURI());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
