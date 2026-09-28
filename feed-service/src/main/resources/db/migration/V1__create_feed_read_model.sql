-- Modelo de lectura (CQRS): proyección desnormalizada de las publicaciones.
CREATE TABLE feed_item
(
    post_id             UUID PRIMARY KEY,
    author_id           UUID         NOT NULL,
    author_username     VARCHAR(50)  NOT NULL,
    author_display_name VARCHAR(100) NOT NULL,
    message             VARCHAR(280) NOT NULL,
    published_at        TIMESTAMPTZ  NOT NULL
);

-- Soporta la paginación keyset ORDER BY published_at DESC, post_id DESC
CREATE INDEX idx_feed_item_published ON feed_item (published_at DESC, post_id DESC);

CREATE TABLE processed_event
(
    event_id     UUID PRIMARY KEY,
    event_type   VARCHAR(100) NOT NULL,
    processed_at TIMESTAMPTZ  NOT NULL
);
