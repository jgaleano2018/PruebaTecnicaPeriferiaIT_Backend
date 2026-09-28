package com.periferia.social.auth.infrastructure.adapter.in.web;

import com.periferia.social.auth.domain.port.in.AuthenticateUserUseCase;
import com.periferia.social.auth.domain.port.in.AuthenticateUserUseCase.Credentials;
import com.periferia.social.auth.domain.port.in.GetUserProfileUseCase;
import com.periferia.social.auth.infrastructure.adapter.in.web.dto.LoginRequest;
import com.periferia.social.auth.infrastructure.adapter.in.web.dto.TokenResponse;
import com.periferia.social.auth.infrastructure.adapter.in.web.dto.UserResponse;
import com.periferia.social.platform.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST para autenticación. */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Login con usuario/clave y emisión de JWT")
class AuthController {

    private final AuthenticateUserUseCase authenticateUser;
    private final GetUserProfileUseCase getUserProfile;

    AuthController(AuthenticateUserUseCase authenticateUser, GetUserProfileUseCase getUserProfile) {
        this.authenticateUser = authenticateUser;
        this.getUserProfile = getUserProfile;
    }

    @GetMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Login (GET) con cabecera Authorization: Basic",
            description = "Envíe `Authorization: Basic base64(usuario:clave)`. Las credenciales nunca viajan en la URL.")
    @ApiResponse(responseCode = "200", description = "JWT emitido")
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    ResponseEntity<TokenResponse> loginWithBasicHeader(
            @Parameter(in = ParameterIn.HEADER, name = HttpHeaders.AUTHORIZATION, example = "Basic YWxpY2U6UGFzc3dvcmQxMjMq")
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return issue(BasicAuthorizationParser.parse(authorization));
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Login (POST) con cuerpo JSON")
    @ApiResponse(responseCode = "200", description = "JWT emitido")
    @ApiResponse(responseCode = "400", description = "Petición inválida")
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return issue(new Credentials(request.username(), request.password()));
    }

    @GetMapping("/me")
    @Operation(summary = "Perfil del usuario autenticado")
    UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return UserResponse.from(getUserProfile.getProfile(AuthenticatedUser.from(jwt).id()));
    }

    private ResponseEntity<TokenResponse> issue(Credentials credentials) {
        TokenResponse body = TokenResponse.from(authenticateUser.authenticate(credentials));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(body);
    }
}
