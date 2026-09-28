package com.periferia.social.post.application.service;

import com.periferia.social.post.domain.exception.DuplicateIdempotencyKeyException;
import com.periferia.social.post.domain.model.IdempotencyRecord;
import com.periferia.social.post.domain.model.Post;
import com.periferia.social.post.domain.model.PostMessage;
import com.periferia.social.post.domain.port.in.CreatePostUseCase;
import com.periferia.social.post.domain.port.out.IdempotencyStore;
import com.periferia.social.post.domain.port.out.PostEventPublisher;
import com.periferia.social.post.domain.port.out.PostRepository;
import com.periferia.social.post.domain.port.out.UnitOfWork;
import com.periferia.social.shared.exception.BusinessRuleViolationException;
import com.periferia.social.shared.exception.IdempotencyKeyReusedException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Crea publicaciones garantizando:
 * <ol>
 *   <li><b>Atomicidad</b>: publicación + registro de idempotencia + evento outbox en una transacción.</li>
 *   <li><b>Idempotencia</b>: reintentos con la misma {@code Idempotency-Key} devuelven la misma
 *       publicación; reutilizar la clave con otro contenido produce 409.</li>
 *   <li><b>Concurrencia</b>: si dos peticiones con la misma clave compiten, la restricción única en BD
 *       decide la ganadora y la perdedora responde con el resultado de la ganadora.</li>
 * </ol>
 */
public class CreatePostService implements CreatePostUseCase {

    static final int MAX_IDEMPOTENCY_KEY_LENGTH = 100;

    private final PostRepository postRepository;
    private final IdempotencyStore idempotencyStore;
    private final PostEventPublisher eventPublisher;
    private final UnitOfWork unitOfWork;
    private final Clock clock;

    public CreatePostService(PostRepository postRepository, IdempotencyStore idempotencyStore,
                             PostEventPublisher eventPublisher, UnitOfWork unitOfWork, Clock clock) {
        this.postRepository = postRepository;
        this.idempotencyStore = idempotencyStore;
        this.eventPublisher = eventPublisher;
        this.unitOfWork = unitOfWork;
        this.clock = clock;
    }

    @Override
    public CreatePostResult create(CreatePostCommand command) {
        PostMessage message = new PostMessage(command.message());
        String key = normalizeKey(command.idempotencyKey());
        if (key == null) {
            return new CreatePostResult(unitOfWork.inTransaction(() -> persist(command, message)), false);
        }
        String requestHash = sha256(message.value());
        Optional<CreatePostResult> previous = replay(command, key, requestHash);
        if (previous.isPresent()) {
            return previous.get();
        }
        try {
            Post post = unitOfWork.inTransaction(() -> {
                Post created = persist(command, message);
                idempotencyStore.save(new IdempotencyRecord(
                        command.author().id(), key, requestHash, created.id(), created.publishedAt()));
                return created;
            });
            return new CreatePostResult(post, false);
        } catch (DuplicateIdempotencyKeyException race) {
            return replay(command, key, requestHash).orElseThrow(() -> race);
        }
    }

    private Post persist(CreatePostCommand command, PostMessage message) {
        Post post = Post.publish(command.author(), message, clock.instant());
        Post saved = postRepository.save(post);
        eventPublisher.postCreated(saved);
        return saved;
    }

    private Optional<CreatePostResult> replay(CreatePostCommand command, String key, String requestHash) {
        return idempotencyStore.find(command.author().id(), key).map(record -> {
            if (!record.matches(requestHash)) {
                throw new IdempotencyKeyReusedException(key);
            }
            Post original = postRepository.findById(record.postId())
                    .orElseThrow(() -> new IllegalStateException("Publicación idempotente no encontrada: " + record.postId()));
            return new CreatePostResult(original, true);
        });
    }

    private static String normalizeKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String trimmed = key.strip();
        if (trimmed.length() > MAX_IDEMPOTENCY_KEY_LENGTH) {
            throw new BusinessRuleViolationException(
                    "La Idempotency-Key no puede superar " + MAX_IDEMPOTENCY_KEY_LENGTH + " caracteres");
        }
        return trimmed;
    }

    static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
