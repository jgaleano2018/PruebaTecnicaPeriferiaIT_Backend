package com.periferia.social.feed.infrastructure.config;

import com.periferia.social.feed.application.service.FeedProjectionService;
import com.periferia.social.feed.application.service.FeedQueryService;
import com.periferia.social.feed.application.service.FeedStreamService;
import com.periferia.social.feed.domain.port.in.GetFeedUseCase;
import com.periferia.social.feed.domain.port.in.ProjectPostUseCase;
import com.periferia.social.feed.domain.port.in.StreamFeedUseCase;
import com.periferia.social.feed.domain.port.out.FeedItemRepository;
import com.periferia.social.feed.domain.port.out.FeedNotifier;
import com.periferia.social.feed.domain.port.out.ProcessedEventRepository;
import com.periferia.social.feed.domain.port.out.ReactiveUnitOfWork;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

/** Composition root de feed-service. */
@Configuration(proxyBeanMethods = false)
class UseCaseConfig {

    @Bean
    ReactiveUnitOfWork reactiveUnitOfWork(TransactionalOperator transactionalOperator) {
        return new ReactiveUnitOfWork() {
            @Override
            public <T> Mono<T> inTransaction(Mono<T> work) {
                return transactionalOperator.transactional(work);
            }
        };
    }

    @Bean
    GetFeedUseCase getFeedUseCase(FeedItemRepository repository) {
        return new FeedQueryService(repository);
    }

    @Bean
    ProjectPostUseCase projectPostUseCase(FeedItemRepository repository, ProcessedEventRepository processedEvents,
                                          ReactiveUnitOfWork unitOfWork) {
        return new FeedProjectionService(repository, processedEvents, unitOfWork);
    }

    @Bean
    StreamFeedUseCase streamFeedUseCase(FeedNotifier notifier) {
        return new FeedStreamService(notifier);
    }
}
