package com.pulselibrary.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public abstract class MediaItem {
    private final String id;
    private final String title;
    private final Instant createdAt;

    protected MediaItem(String id, String title, Instant createdAt) {
        this.id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
        this.title = Objects.requireNonNull(title, "title").trim();
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

