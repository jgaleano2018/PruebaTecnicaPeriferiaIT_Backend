package com.periferia.social.post.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.periferia.social.platform.security.JwtResourceServerConfig;
import com.periferia.social.platform.security.ProblemDetailSecurityHandlers;
import com.periferia.social.post.domain.model.Author;
import com.periferia.social.post.domain.model.Post;
import com.periferia.social.post.domain.model.PostMessage;
import com.periferia.social.post.domain.port.in.CreatePostUseCase;
import com.periferia.social.post.domain.port.in.CreatePostUseCase.CreatePostResult;
import com.periferia.social.post.domain.port.in.GetPostUseCase;
import com.periferia.social.post.infrastructure.config.SecurityConfig;
import com.periferia.social.shared.exception.NotFoundException;
import com.periferia.social.shared.security.JwtClaimNames;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(PostController.class)
@Import({SecurityConfig.class, JwtResourceServerConfig.class, ProblemDetailSecurityHandlers.class})
@TestPropertySource(properties = {
        "app.security.jwt.secret=test-secret-with-at-least-32-characters!!",
        "app.security.jwt.issuer=test-issuer"
})
class PostControllerTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreatePostUseCase createPost;
    @MockitoBean
    private GetPostUseCase getPost;

    @Test
    void createsPostUsingIdentityFromJwt() throws Exception {
        Post post = Post.publish(new Author(USER_ID, "alice", "Alice"), new PostMessage("Hola"), Instant.now());
        when(createPost.create(argThat(cmd -> cmd.author().id().equals(USER_ID)
                && "key-1".equals(cmd.idempotencyKey())))).thenReturn(new CreatePostResult(post, false));

        mockMvc.perform(post("/api/v1/posts")
                        .with(aliceJwt())
                        .header("Idempotency-Key", "key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Hola\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Idempotent-Replayed", "false"))
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.message").value("Hola"))
                .andExpect(jsonPath("$.author.username").value("alice"));
    }

    @Test
    void replayedRequestReturns200() throws Exception {
        Post post = Post.publish(new Author(USER_ID, "alice", "Alice"), new PostMessage("Hola"), Instant.now());
        when(createPost.create(any())).thenReturn(new CreatePostResult(post, true));

        mockMvc.perform(post("/api/v1/posts").with(aliceJwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"Hola\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Idempotent-Replayed", "true"));
    }

    @Test
    void rejectsBlankMessage() throws Exception {
        mockMvc.perform(post("/api/v1/posts").with(aliceJwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0].field").value("message"));
    }

    @Test
    void requiresJwt() throws Exception {
        mockMvc.perform(post("/api/v1/posts").contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownPostReturns404ProblemDetail() throws Exception {
        UUID id = UUID.randomUUID();
        when(getPost.getById(id)).thenThrow(new NotFoundException("Publicación no encontrada"));

        mockMvc.perform(get("/api/v1/posts/{id}", id).with(aliceJwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    private static RequestPostProcessor aliceJwt() {
        return jwt().jwt(j -> j.subject(USER_ID.toString())
                .claim(JwtClaimNames.USERNAME, "alice")
                .claim(JwtClaimNames.DISPLAY_NAME, "Alice"));
    }
}
