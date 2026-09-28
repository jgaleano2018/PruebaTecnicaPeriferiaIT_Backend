package com.periferia.social.auth.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.periferia.social.auth.domain.model.AccessToken;
import com.periferia.social.auth.domain.model.AuthenticationResult;
import com.periferia.social.auth.domain.model.UserProfile;
import com.periferia.social.auth.domain.port.in.AuthenticateUserUseCase;
import com.periferia.social.auth.domain.port.in.GetUserProfileUseCase;
import com.periferia.social.auth.infrastructure.config.SecurityConfig;
import com.periferia.social.platform.security.JwtResourceServerConfig;
import com.periferia.social.platform.security.ProblemDetailSecurityHandlers;
import com.periferia.social.shared.exception.InvalidCredentialsException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtResourceServerConfig.class, ProblemDetailSecurityHandlers.class})
@TestPropertySource(properties = {
        "app.security.jwt.secret=test-secret-with-at-least-32-characters!!",
        "app.security.jwt.issuer=test-issuer"
})
class AuthControllerTest {

    private static final UserProfile ALICE = new UserProfile(UUID.randomUUID(), "alice", "Alice");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticateUserUseCase authenticateUser;
    @MockitoBean
    private GetUserProfileUseCase getUserProfile;

    @Test
    void loginWithGetAndBasicHeaderReturnsToken() throws Exception {
        when(authenticateUser.authenticate(new AuthenticateUserUseCase.Credentials("alice", "Password123*")))
                .thenReturn(result());
        String basic = Base64.getEncoder().encodeToString("alice:Password123*".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/v1/auth/login").header(HttpHeaders.AUTHORIZATION, "Basic " + basic))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("alice"));
    }

    @Test
    void loginWithPostAndJsonBodyReturnsToken() throws Exception {
        when(authenticateUser.authenticate(any())).thenReturn(result());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"Password123*\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"));
    }

    @Test
    void invalidCredentialsReturnProblemDetail401() throws Exception {
        when(authenticateUser.authenticate(any())).thenThrow(new InvalidCredentialsException("Usuario o clave incorrectos"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"bad\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.detail").value("Usuario o clave incorrectos"));
    }

    @Test
    void invalidBodyReturnsValidationErrors() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void meRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private static AuthenticationResult result() {
        return new AuthenticationResult(new AccessToken("jwt-token", "Bearer", Instant.now().plusSeconds(3600)), ALICE);
    }
}
