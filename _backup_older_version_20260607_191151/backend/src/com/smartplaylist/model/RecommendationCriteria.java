package com.smartplaylist.model;

public final class RecommendationCriteria {
    private final String username;
    private final String genre;
    private final String mood;
    private final int limit;

    public RecommendationCriteria(String username, String genre, String mood, int limit) {
        this.username = username == null ? "" : username.trim();
        this.genre = genre == null || genre.isBlank() ? "Any" : genre.trim();
        this.mood = mood == null || mood.isBlank() ? "Any" : mood.trim();
        this.limit = limit;
    }

    public String getUsername() {
        return username;
    }

    public String getGenre() {
        return genre;
    }

    public String getMood() {
        return mood;
    }

    public int getLimit() {
        return limit;
    }
}
