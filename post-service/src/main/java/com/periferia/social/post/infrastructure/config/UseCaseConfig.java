package com.periferia.social.post.infrastructure.config;

import com.periferia.social.platform.UnitOfWorkAdapter;
import com.periferia.social.post.application.service.CreatePostService;
import com.periferia.social.post.application.service.GetPostService;
import com.periferia.social.post.application.service.WelcomePostService;
import com.periferia.social.post.domain.port.in.CreatePostUseCase;
import com.periferia.social.post.domain.port.in.CreateWelcomePostUseCase;
import com.periferia.social.post.domain.port.in.GetPostUseCase;
import com.periferia.social.post.domain.port.out.IdempotencyStore;
import com.periferia.social.post.domain.port.out.PostEventPublisher;
import com.periferia.social.post.domain.port.out.PostRepository;
import com.periferia.social.post.domain.port.out.ProcessedEventStore;
import com.periferia.social.post.domain.port.out.UnitOfWork;
import java.time.Clock;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Composition root de post-service. */
@Configuration(proxyBeanMethods = false)
class UseCaseConfig {

    @Bean
    UnitOfWork unitOfWork(UnitOfWorkAdapter adapter) {
        return new UnitOfWork() {
            @Override
            public <T> T inTransaction(Supplier<T> work) {
                return adapter.inTransaction(work);
            }
        };
    }

    @Bean
    CreatePostUseCase createPostUseCase(PostRepository posts, IdempotencyStore idempotencyStore,
                                        PostEventPublisher events, UnitOfWork unitOfWork, Clock clock) {
        return new CreatePostService(posts, idempotencyStore, events, unitOfWork, clock);
    }

    @Bean
    GetPostUseCase getPostUseCase(PostRepository posts) {
        return new GetPostService(posts);
    }

    @Bean
    CreateWelcomePostUseCase createWelcomePostUseCase(PostRepository posts, PostEventPublisher events,
                                                      ProcessedEventStore processedEvents, UnitOfWork unitOfWork,
                                                      Clock clock,
                                                      @Value("${app.seed.welcome-message}") String template) {
        return new WelcomePostService(posts, events, processedEvents, unitOfWork, clock, template);
    }
}
