package com.smartplaylist.service;

import com.smartplaylist.exception.ValidationException;
import com.smartplaylist.model.Playlist;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PlaylistStore {
    private final Map<String, List<Playlist>> playlistsByUser = new ConcurrentHashMap<>();
    private final Object writeLock = new Object();

    public void saveForUser(String username, Playlist playlist) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("username is required.");
        }
        if (playlist == null) {
            throw new ValidationException("playlist is required.");
        }
        if (!playlist.getUsername().equalsIgnoreCase(username.trim())) {
            throw new ValidationException("You can only save playlists for the active user.");
        }

        synchronized (writeLock) {
            List<Playlist> saved = playlistsByUser.computeIfAbsent(normalize(username), key -> new ArrayList<>());
            boolean alreadySaved = saved.stream().anyMatch(existing -> existing.getId().equals(playlist.getId()));
            if (alreadySaved) {
                throw new ValidationException("This playlist has already been saved.");
            }
            saved.add(0, playlist);
        }
    }

    public List<Playlist> listForUser(String username) {
        synchronized (writeLock) {
            return List.copyOf(playlistsByUser.getOrDefault(normalize(username), List.of()));
        }
    }

    public int countForUser(String username) {
        return listForUser(username).size();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
