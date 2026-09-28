package com.periferia.social.feed.domain.port.out;

import reactor.core.publisher.Mono;

public interface ReactiveUnitOfWork {

    <T> Mono<T> inTransaction(Mono<T> work);
}
