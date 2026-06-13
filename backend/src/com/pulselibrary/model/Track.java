package com.pulselibrary.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Track extends MediaItem {
    private final String artist;
    private final String genre;
    private final List<MoodType> moods;
    private final int durationSeconds;
    private final int energy;
    private final int popularity;
    private final String accent;
    private final String previewUrl;
    private final String previewTrackName;
    private final String previewArtist;

    private Track(Builder builder) {
        super(builder.id, builder.title, builder.createdAt);
        this.artist = builder.artist;
        this.genre = builder.genre;
        this.moods = List.copyOf(builder.moods);
        this.durationSeconds = builder.durationSeconds;
        this.energy = builder.energy;
        this.popularity = builder.popularity;
        this.accent = builder.accent;
        this.previewUrl = builder.previewUrl;
        this.previewTrackName = builder.previewTrackName;
        this.previewArtist = builder.previewArtist;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }

    public List<MoodType> getMoods() {
        return moods;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public int getEnergy() {
        return energy;
    }

    public int getPopularity() {
        return popularity;
    }

    public String getAccent() {
        return accent;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public String getPreviewTrackName() {
        return previewTrackName;
    }

    public String getPreviewArtist() {
        return previewArtist;
    }

    public boolean hasMood(MoodType mood) {
        return mood == null || moods.contains(mood);
    }

    public String durationLabel() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", getId());
        map.put("title", getTitle());
        map.put("artist", artist);
        map.put("genre", genre);
        map.put("moods", moods.stream().map(MoodType::getLabel).toList());
        map.put("duration", durationSeconds);
        map.put("durationLabel", durationLabel());
        map.put("energy", energy);
        map.put("popularity", popularity);
        map.put("accent", accent);
        map.put("previewUrl", previewUrl);
        map.put("previewTrackName", previewTrackName);
        map.put("previewArtist", previewArtist);
        return map;
    }

    public static final class Builder {
        private String id;
        private String title;
        private String artist;
        private String genre;
        private final List<MoodType> moods = new ArrayList<>();
        private int durationSeconds = 200;
        private int energy = 5;
        private int popularity = 80;
        private String accent = "#f73755";
        private String previewUrl;
        private String previewTrackName;
        private String previewArtist;
        private Instant createdAt;

        public Builder id(String value) {
            this.id = value;
            return this;
        }

        public Builder title(String value) {
            this.title = value;
            return this;
        }

        public Builder artist(String value) {
            this.artist = value;
            return this;
        }

        public Builder genre(String value) {
            this.genre = value;
            return this;
        }

        public Builder mood(MoodType value) {
            if (value != null && !moods.contains(value)) {
                moods.add(value);
            }
            return this;
        }

        public Builder moods(List<MoodType> values) {
            moods.clear();
            if (values != null) {
                moods.addAll(values);
            }
            return this;
        }

        public Builder duration(int seconds) {
            this.durationSeconds = seconds;
            return this;
        }

        public Builder energy(int value) {
            this.energy = value;
            return this;
        }

        public Builder popularity(int value) {
            this.popularity = value;
            return this;
        }

        public Builder accent(String value) {
            this.accent = value;
            return this;
        }

        public Builder previewUrl(String value) {
            this.previewUrl = value;
            return this;
        }

        public Builder previewSource(String trackName, String artistName) {
            this.previewTrackName = trackName;
            this.previewArtist = artistName;
            return this;
        }

        public Track build() {
            Objects.requireNonNull(title, "title");
            Objects.requireNonNull(artist, "artist");
            Objects.requireNonNull(genre, "genre");
            if (moods.isEmpty()) {
                throw new IllegalStateException("At least one mood is required");
            }
            return new Track(this);
        }
    }
}
