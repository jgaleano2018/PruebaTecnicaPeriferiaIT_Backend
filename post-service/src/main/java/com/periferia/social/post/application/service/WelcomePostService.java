package com.periferia.social.post.application.service;

import com.periferia.social.post.domain.model.Author;
import com.periferia.social.post.domain.model.Post;
import com.periferia.social.post.domain.model.PostMessage;
import com.periferia.social.post.domain.port.in.CreateWelcomePostUseCase;
import com.periferia.social.post.domain.port.out.PostEventPublisher;
import com.periferia.social.post.domain.port.out.PostRepository;
import com.periferia.social.post.domain.port.out.ProcessedEventStore;
import com.periferia.social.post.domain.port.out.UnitOfWork;
import com.periferia.social.shared.event.UserRegisteredEvent;
import java.time.Clock;
import java.util.UUID;

/**
 * Idempotent Consumer: el registro en {@code processed_event} y la publicación se guardan en
 * la misma transacción; si Kafka re-entrega el evento, no se duplica la publicación.
 */
public class WelcomePostService implements CreateWelcomePostUseCase {

    private final PostRepository postRepository;
    private final PostEventPublisher eventPublisher;
    private final ProcessedEventStore processedEvents;
    private final UnitOfWork unitOfWork;
    private final Clock clock;
    private final String messageTemplate;

    public WelcomePostService(PostRepository postRepository, PostEventPublisher eventPublisher,
                              ProcessedEventStore processedEvents, UnitOfWork unitOfWork, Clock clock,
                              String messageTemplate) {
        this.postRepository = postRepository;
        this.eventPublisher = eventPublisher;
        this.processedEvents = processedEvents;
        this.unitOfWork = unitOfWork;
        this.clock = clock;
        this.messageTemplate = messageTemplate;
    }

    @Override
    public boolean handle(UUID eventId, Author author) {
        return unitOfWork.inTransaction(() -> {
            if (!processedEvents.markProcessed(eventId, UserRegisteredEvent.TYPE)) {
                return false;
            }
            PostMessage message = new PostMessage(messageTemplate.formatted(author.displayName()));
            Post post = postRepository.save(Post.publish(author, message, clock.instant()));
            eventPublisher.postCreated(post);
            return true;
        });
    }
}
