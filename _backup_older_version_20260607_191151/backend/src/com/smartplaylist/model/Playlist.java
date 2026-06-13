package com.smartplaylist.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Playlist {
    private final String id;
    private final String name;
    private final String username;
    private final String genre;
    private final String mood;
    private final String createdAt;
    private final String summary;
    private final String accent;
    private final List<Song> songs;

    public Playlist(
        String id,
        String name,
        String username,
        String genre,
        String mood,
        String createdAt,
        String summary,
        String accent,
        List<Song> songs
    ) {
        this.id = required(id, "id");
        this.name = required(name, "name");
        this.username = required(username, "username");
        this.genre = required(genre, "genre");
        this.mood = required(mood, "mood");
        this.createdAt = required(createdAt, "createdAt");
        this.summary = required(summary, "summary");
        this.accent = required(accent, "accent");
        this.songs = List.copyOf(Objects.requireNonNull(songs, "songs"));
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("username", username);
        map.put("genre", genre);
        map.put("mood", mood);
        map.put("createdAt", createdAt);
        map.put("summary", summary);
        map.put("accent", accent);
        map.put("trackCount", songs.size());
        map.put("totalDurationLabel", totalDurationLabel());
        map.put("songs", songs.stream().map(song -> song.toMap()).toList());
        return map;
    }

    private String totalDurationLabel() {
        int totalSeconds = songs.stream().mapToInt(Song::getDurationSeconds).sum();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private static String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}
