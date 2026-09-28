CREATE TABLE posts
(
    id                  UUID PRIMARY KEY,
    author_id           UUID         NOT NULL,
    author_username     VARCHAR(50)  NOT NULL,
    author_display_name VARCHAR(100) NOT NULL,
    message             VARCHAR(280) NOT NULL,
    published_at        TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_posts_author_published ON posts (author_id, published_at DESC);

-- Idempotencia de la API (cabecera Idempotency-Key)
CREATE TABLE idempotency_key
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID         NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    request_hash    VARCHAR(64)  NOT NULL,
    post_id         UUID         NOT NULL REFERENCES posts (id),
    created_at      TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_idempotency_user_key UNIQUE (user_id, idempotency_key)
);

-- Idempotent Consumer (eventos Kafka ya procesados)
CREATE TABLE processed_event
(
    event_id     UUID PRIMARY KEY,
    event_type   VARCHAR(100) NOT NULL,
    processed_at TIMESTAMPTZ  NOT NULL
);

-- Transactional Outbox
CREATE TABLE outbox_event
(
    id           UUID PRIMARY KEY,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type   VARCHAR(100) NOT NULL,
    topic        VARCHAR(200) NOT NULL,
    payload      TEXT         NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    published_at TIMESTAMPTZ,
    attempts     INT          NOT NULL DEFAULT 0,
    last_error   VARCHAR(1000)
);

CREATE INDEX idx_outbox_event_pending ON outbox_event (created_at) WHERE published_at IS NULL;
