package com.periferia.social.post.infrastructure.adapter.in.web.dto;

import com.periferia.social.post.domain.model.PostMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solo se recibe el mensaje: el usuario sale del JWT y la fecha de publicación se asigna
 * por defecto al guardar.
 */
@Schema(description = "Datos para crear una publicación")
public record CreatePostRequest(
        @Schema(example = "¡Hola mundo desde la red social!", maxLength = PostMessage.MAX_LENGTH)
        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = PostMessage.MAX_LENGTH, message = "El mensaje no puede superar {max} caracteres")
        String message) {
}
