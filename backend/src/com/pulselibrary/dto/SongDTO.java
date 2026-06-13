package com.pulselibrary.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SongDTO {
    private Long id;
    private String title;
    private String artist;
    private String album;
    private String artworkUrl;
    private String previewUrl;
    private Long duration;
    private String genre;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtworkUrl() {
        return artworkUrl;
    }

    public void setArtworkUrl(String artworkUrl) {
        this.artworkUrl = artworkUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String durationLabel() {
        if (duration == null || duration <= 0) {
            return "0:00";
        }
        long totalSeconds = duration / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id == null ? null : String.valueOf(id));
        map.put("title", title);
        map.put("artist", artist);
        map.put("album", album);
        map.put("artworkUrl", artworkUrl);
        map.put("previewUrl", previewUrl);
        map.put("duration", duration);
        map.put("durationLabel", durationLabel());
        map.put("genre", genre);
        return map;
    }
}
