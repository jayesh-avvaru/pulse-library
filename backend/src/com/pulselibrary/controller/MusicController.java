package com.pulselibrary.controller;

import com.pulselibrary.dto.SongDTO;
import com.pulselibrary.service.ITunesService;
import com.pulselibrary.util.HttpResponses;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public final class MusicController {
    private final ITunesService iTunesService;

    public MusicController(ITunesService iTunesService) {
        this.iTunesService = iTunesService;
    }

    public void handleSearch(HttpExchange exchange) throws IOException {
        if (preflight(exchange)) {
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.error(exchange, 405, "Only GET is supported");
            return;
        }
        String query = queryValue(exchange.getRequestURI(), "q");
        int limit = parseInt(queryValue(exchange.getRequestURI(), "limit"), 20);
        try {
            respondSongs(exchange, iTunesService.search(query == null ? "" : query, limit));
        } catch (Exception ex) {
            HttpResponses.error(exchange, 502, "Could not load songs from iTunes: " + ex.getMessage());
        }
    }

    public void handleGenre(HttpExchange exchange) throws IOException {
        if (preflight(exchange)) {
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.error(exchange, 405, "Only GET is supported");
            return;
        }
        String genre = queryValue(exchange.getRequestURI(), "name");
        int limit = parseInt(queryValue(exchange.getRequestURI(), "limit"), 20);
        try {
            respondSongs(exchange, iTunesService.byGenre(genre == null ? "Pop" : genre, limit));
        } catch (Exception ex) {
            HttpResponses.error(exchange, 502, "Could not load songs from iTunes: " + ex.getMessage());
        }
    }

    public void handlePlaylist(HttpExchange exchange) throws IOException {
        if (preflight(exchange)) {
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.error(exchange, 405, "Only GET is supported");
            return;
        }
        String mood = queryValue(exchange.getRequestURI(), "mood");
        int limit = parseInt(queryValue(exchange.getRequestURI(), "limit"), 15);
        try {
            respondSongs(exchange, iTunesService.playlistByMood(mood == null ? "chill" : mood, limit));
        } catch (Exception ex) {
            HttpResponses.error(exchange, 502, "Could not load songs from iTunes: " + ex.getMessage());
        }
    }

    public void handleRecommend(HttpExchange exchange) throws IOException {
        if (preflight(exchange)) {
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.error(exchange, 405, "Only GET is supported");
            return;
        }
        String artist = queryValue(exchange.getRequestURI(), "artist");
        int limit = parseInt(queryValue(exchange.getRequestURI(), "limit"), 10);
        try {
            respondSongs(exchange, iTunesService.recommendByArtist(artist == null ? "" : artist, limit));
        } catch (Exception ex) {
            HttpResponses.error(exchange, 502, "Could not load songs from iTunes: " + ex.getMessage());
        }
    }

    public void handleTrending(HttpExchange exchange) throws IOException {
        if (preflight(exchange)) {
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.error(exchange, 405, "Only GET is supported");
            return;
        }
        int limit = parseInt(queryValue(exchange.getRequestURI(), "limit"), 20);
        try {
            respondSongs(exchange, iTunesService.trending(limit));
        } catch (Exception ex) {
            HttpResponses.error(exchange, 502, "Could not load songs from iTunes: " + ex.getMessage());
        }
    }

    public void handleGenres(HttpExchange exchange) throws IOException {
        if (preflight(exchange)) {
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.error(exchange, 405, "Only GET is supported");
            return;
        }
        HttpResponses.json(exchange, 200, iTunesService.presetGenres());
    }

    private void respondSongs(HttpExchange exchange, List<SongDTO> songs) throws IOException {
        List<Object> payload = songs.stream().map(SongDTO::toMap).collect(Collectors.toList());
        HttpResponses.json(exchange, 200, payload);
    }

    private boolean preflight(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.noContent(exchange, 204);
            return true;
        }
        return false;
    }

    private String queryValue(URI uri, String key) {
        if (uri.getQuery() == null) {
            return null;
        }
        for (String part : uri.getQuery().split("&")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2 && pair[0].equalsIgnoreCase(key)) {
                return java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return fallback;
        }
    }
}
