package com.pulselibrary.controller;

import com.pulselibrary.model.MoodType;
import com.pulselibrary.model.GeneratedPlaylist;
import com.pulselibrary.model.Playlist;
import com.pulselibrary.service.PlaylistService;
import com.pulselibrary.util.HttpResponses;
import com.pulselibrary.util.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class PlaylistController {
    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    public void handleGenerate(HttpExchange exchange) throws IOException {
        if (preflight(exchange)) {
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.error(exchange, 405, "Only POST is supported");
            return;
        }

        Map<String, String> body = JsonParser.parseObject(HttpResponses.readBody(exchange));
        String genre = body.getOrDefault("genre", "Any");
        String moodRaw = body.getOrDefault("mood", "Any");
        int trackCount = parseInt(body.get("trackCount"), 6);

        MoodType mood = null;
        if (moodRaw != null && !moodRaw.isBlank() && !"ANY".equalsIgnoreCase(moodRaw)) {
            try {
                mood = MoodType.fromString(moodRaw);
            } catch (IllegalArgumentException ex) {
                HttpResponses.error(exchange, 400, ex.getMessage());
                return;
            }
        }

        GeneratedPlaylist playlist = playlistService.generate(genre, mood, trackCount);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("playlist", playlist.toMap());
        payload.put("message", "Playlist generated");
        HttpResponses.json(exchange, 200, payload);
    }

    public void handleSaved(HttpExchange exchange) throws IOException {
        if (preflight(exchange)) {
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.error(exchange, 405, "Only GET is supported");
            return;
        }

        List<Map<String, Object>> playlists = playlistService.getAllSaved().stream()
            .map(Playlist::toMap)
            .collect(Collectors.toList());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("playlists", playlists);
        HttpResponses.json(exchange, 200, payload);
    }

    public void handleDelete(HttpExchange exchange, String playlistId) throws IOException {
        if (preflight(exchange)) {
            return;
        }
        if (!"DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.error(exchange, 405, "Only DELETE is supported");
            return;
        }

        boolean removed = playlistService.delete(playlistId);
        if (!removed) {
            HttpResponses.error(exchange, 404, "Playlist not found");
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", "Playlist deleted");
        HttpResponses.json(exchange, 200, payload);
    }

    public void handleSaveFromBody(HttpExchange exchange) throws IOException {
        if (preflight(exchange)) {
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.error(exchange, 405, "Only POST is supported");
            return;
        }

        Map<String, String> flat = JsonParser.parseObject(HttpResponses.readBody(exchange));
        String id = flat.get("id");
        if (id == null || id.isBlank()) {
            HttpResponses.error(exchange, 400, "Playlist id is required to save.");
            return;
        }
        try {
            playlistService.saveById(id);
        } catch (IllegalArgumentException ex) {
            HttpResponses.error(exchange, 400, ex.getMessage());
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", "Playlist saved to your shelf");
        HttpResponses.json(exchange, 200, payload);
    }

    private boolean preflight(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.noContent(exchange, 204);
            return true;
        }
        return false;
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return fallback;
        }
    }
}

