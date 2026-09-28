package com.periferia.social.post.infrastructure.adapter.in.web;

import com.periferia.social.platform.security.AuthenticatedUser;
import com.periferia.social.post.domain.model.Author;
import com.periferia.social.post.domain.port.in.CreatePostUseCase;
import com.periferia.social.post.domain.port.in.CreatePostUseCase.CreatePostCommand;
import com.periferia.social.post.domain.port.in.CreatePostUseCase.CreatePostResult;
import com.periferia.social.post.domain.port.in.GetPostUseCase;
import com.periferia.social.post.infrastructure.adapter.in.web.dto.CreatePostRequest;
import com.periferia.social.post.infrastructure.adapter.in.web.dto.PostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Publicaciones (comandos)", description = "Creación de publicaciones. El listado lo expone feed-service (CQRS).")
class PostController {

    static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    static final String IDEMPOTENT_REPLAYED_HEADER = "Idempotent-Replayed";

    private final CreatePostUseCase createPost;
    private final GetPostUseCase getPost;

    PostController(CreatePostUseCase createPost, GetPostUseCase getPost) {
        this.createPost = createPost;
        this.getPost = getPost;
    }

    @PostMapping
    @Operation(summary = "Crear publicación",
            description = "El autor se toma del JWT y la fecha de publicación se asigna al guardar. "
                    + "Envíe `Idempotency-Key` para que los reintentos no dupliquen la publicación.")
    @ApiResponse(responseCode = "201", description = "Publicación creada")
    @ApiResponse(responseCode = "200", description = "Reintento idempotente: se devuelve la publicación original")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "409", description = "Idempotency-Key reutilizada con otro contenido")
    ResponseEntity<PostResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Clave única por intento lógico (p. ej. UUID v4)")
            @RequestHeader(value = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody CreatePostRequest request) {
        AuthenticatedUser user = AuthenticatedUser.from(jwt);
        CreatePostResult result = createPost.create(new CreatePostCommand(
                new Author(user.id(), user.username(), user.displayName()), request.message(), idempotencyKey));
        PostResponse body = PostResponse.from(result.post());
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.status(result.replayed() ? HttpStatus.OK : HttpStatus.CREATED)
                .location(location)
                .header(IDEMPOTENT_REPLAYED_HEADER, String.valueOf(result.replayed()))
                .body(body);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una publicación por id")
    @ApiResponse(responseCode = "404", description = "No existe")
    PostResponse getById(@PathVariable UUID id) {
        return PostResponse.from(getPost.getById(id));
    }
}
