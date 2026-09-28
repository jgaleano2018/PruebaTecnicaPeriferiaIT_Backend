package com.periferia.social.post.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "posts")
public class PostJpaEntity {

    @Id
    private UUID id;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "author_username", nullable = false, length = 50)
    private String authorUsername;

    @Column(name = "author_display_name", nullable = false, length = 100)
    private String authorDisplayName;

    @Column(nullable = false, length = 280)
    private String message;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    protected PostJpaEntity() {
        // JPA
    }

    public PostJpaEntity(UUID id, UUID authorId, String authorUsername, String authorDisplayName, String message,
                         Instant publishedAt) {
        this.id = id;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.authorDisplayName = authorDisplayName;
        this.message = message;
        this.publishedAt = publishedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

    public String getMessage() {
        return message;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }
}
