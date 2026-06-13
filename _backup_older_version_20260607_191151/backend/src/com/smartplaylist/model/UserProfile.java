package com.smartplaylist.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class UserProfile {
    private final String username;
    private final String password;
    private final String displayName;
    private final List<String> favoriteGenres;
    private final List<String> favoriteMoods;
    private final List<String> listeningHistory = new ArrayList<>();

    public UserProfile(
        String username,
        String password,
        String displayName,
        List<String> favoriteGenres,
        List<String> favoriteMoods
    ) {
        this.username = required(username, "username");
        this.password = required(password, "password");
        this.displayName = required(displayName, "displayName");
        this.favoriteGenres = List.copyOf(Objects.requireNonNull(favoriteGenres, "favoriteGenres"));
        this.favoriteMoods = List.copyOf(Objects.requireNonNull(favoriteMoods, "favoriteMoods"));
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getFavoriteGenres() {
        return favoriteGenres;
    }

    public List<String> getFavoriteMoods() {
        return favoriteMoods;
    }

    public boolean passwordMatches(String candidatePassword) {
        return password.equals(candidatePassword == null ? "" : candidatePassword.trim());
    }

    public synchronized void rememberPlaylist(String playlistName) {
        if (playlistName == null || playlistName.isBlank()) {
            return;
        }
        listeningHistory.add(playlistName.trim());
        if (listeningHistory.size() > 8) {
            listeningHistory.remove(0);
        }
    }

    public synchronized List<String> getListeningHistory() {
        return List.copyOf(listeningHistory);
    }

    public Map<String, Object> toPublicMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("username", username);
        map.put("displayName", displayName);
        map.put("favoriteGenres", favoriteGenres);
        map.put("favoriteMoods", favoriteMoods);
        return map;
    }

    private static String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}
