package com.pulselibrary.model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GeneratedPlaylist extends Playlist {
    private final String genreFilter;
    private final String moodFilter;
    private final Instant generatedAt;

    public GeneratedPlaylist(
        String id,
        String title,
        List<Track> tracks,
        String genreFilter,
        String moodFilter,
        Instant generatedAt
    ) {
        super(id, title, generatedAt, tracks);
        this.genreFilter = genreFilter == null || genreFilter.isBlank() ? "Any" : genreFilter;
        this.moodFilter = moodFilter == null || moodFilter.isBlank() ? "Any" : moodFilter;
        this.generatedAt = generatedAt == null ? Instant.now() : generatedAt;
    }

    public String getGenreFilter() {
        return genreFilter;
    }

    public String getMoodFilter() {
        return moodFilter;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>(super.toMap());
        map.put("genre", genreFilter);
        map.put("mood", moodFilter);
        map.put("genreFilter", genreFilter);
        map.put("moodFilter", moodFilter);
        map.put("generatedAt", generatedAt.toString());
        map.put(
            "summary",
            "A curated mix tuned to " + moodFilter.toLowerCase() + " energy and " + genreFilter.toLowerCase() + " textures."
        );
        map.put("accent", getTracks().isEmpty() ? "#f73755" : getTracks().get(0).getAccent());
        return map;
    }
}

