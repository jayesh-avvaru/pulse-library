package com.pulselibrary.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Playlist extends MediaItem {
    private final List<Track> tracks;

    protected Playlist(String id, String title, Instant createdAt, List<Track> tracks) {
        super(id, title, createdAt);
        this.tracks = List.copyOf(Objects.requireNonNull(tracks, "tracks"));
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public int getTrackCount() {
        return tracks.size();
    }

    public int getTotalDurationSeconds() {
        return tracks.stream().mapToInt(Track::getDurationSeconds).sum();
    }

    public String getTotalDurationLabel() {
        int total = getTotalDurationSeconds();
        return String.format("%d:%02d", total / 60, total % 60);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", getId());
        map.put("title", getTitle());
        map.put("name", getTitle());
        map.put("createdAt", getCreatedAt().toString());
        map.put("savedAt", getCreatedAt().toString());
        map.put("trackCount", getTrackCount());
        map.put("totalDurationLabel", getTotalDurationLabel());
        map.put("tracks", tracks.stream().map(Track::toMap).toList());
        map.put("songs", tracks.stream().map(Track::toMap).toList());
        return map;
    }

    public static class Builder {
        private String id;
        private String title;
        private Instant createdAt;
        private final List<Track> tracks = new ArrayList<>();

        public Builder id(String value) {
            this.id = value;
            return this;
        }

        public Builder title(String value) {
            this.title = value;
            return this;
        }

        public Builder createdAt(Instant value) {
            this.createdAt = value;
            return this;
        }

        public Builder tracks(List<Track> value) {
            tracks.clear();
            if (value != null) {
                tracks.addAll(value);
            }
            return this;
        }

        public Builder addTrack(Track track) {
            tracks.add(track);
            return this;
        }

        public Playlist build() {
            Objects.requireNonNull(title, "title");
            return new Playlist(id, title, createdAt, tracks);
        }
    }
}

