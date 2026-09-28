package com.periferia.social.feed.domain.port.out;

import com.periferia.social.feed.domain.model.FeedItem;
import reactor.core.publisher.Flux;

/** Canal de difusión en memoria (por instancia) hacia los clientes conectados. */
public interface FeedNotifier {

    void publish(FeedItem item);

    Flux<FeedItem> updates();
}
