package com.periferia.social.auth.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Credenciales de acceso")
public record LoginRequest(
        @Schema(example = "alice") @NotBlank(message = "El usuario es obligatorio") @Size(max = 50) String username,
        @Schema(example = "Password123*") @NotBlank(message = "La clave es obligatoria") @Size(max = 100) String password) {

    @Override
    public String toString() {
        return "LoginRequest[username=" + username + ", password=****]";
    }
}
