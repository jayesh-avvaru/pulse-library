package com.smartplaylist.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Song {
    private final String id;
    private final String title;
    private final String artist;
    private final String genre;
    private final List<String> moods;
    private final int durationSeconds;
    private final int popularity;
    private final int energy;
    private final String accent;

    public Song(
        String id,
        String title,
        String artist,
        String genre,
        List<String> moods,
        int durationSeconds,
        int popularity,
        int energy,
        String accent
    ) {
        this.id = required(id, "id");
        this.title = required(title, "title");
        this.artist = required(artist, "artist");
        this.genre = required(genre, "genre");
        this.moods = List.copyOf(Objects.requireNonNull(moods, "moods"));
        this.durationSeconds = durationSeconds;
        this.popularity = popularity;
        this.energy = energy;
        this.accent = required(accent, "accent");
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }

    public List<String> getMoods() {
        return moods;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public int getPopularity() {
        return popularity;
    }

    public int getEnergy() {
        return energy;
    }

    public String getAccent() {
        return accent;
    }

    public boolean matchesMood(String desiredMood) {
        if (desiredMood == null || desiredMood.isBlank() || "Any".equalsIgnoreCase(desiredMood)) {
            return true;
        }
        return moods.stream().anyMatch(mood -> mood.equalsIgnoreCase(desiredMood.trim()));
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("artist", artist);
        map.put("genre", genre);
        map.put("moods", moods);
        map.put("durationLabel", durationLabel());
        map.put("accent", accent);
        return map;
    }

    private String durationLabel() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private static String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Song song)) {
            return false;
        }
        return id.equals(song.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
