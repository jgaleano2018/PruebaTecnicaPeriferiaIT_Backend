package com.periferia.social.feed.application.service;

import com.periferia.social.feed.domain.model.FeedItem;
import com.periferia.social.feed.domain.port.in.StreamFeedUseCase;
import com.periferia.social.feed.domain.port.out.FeedNotifier;
import java.util.UUID;
import reactor.core.publisher.Flux;

public class FeedStreamService implements StreamFeedUseCase {

    private final FeedNotifier notifier;

    public FeedStreamService(FeedNotifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public Flux<FeedItem> stream(UUID viewerId) {
        return notifier.updates().filter(item -> !item.isAuthoredBy(viewerId));
    }

    @Override
    public void broadcast(FeedItem item) {
        notifier.publish(item);
    }
}
