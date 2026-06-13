package com.pulselibrary.controller;

import com.pulselibrary.model.MoodType;
import com.pulselibrary.model.Track;
import com.pulselibrary.service.MusicService;
import com.pulselibrary.util.HttpResponses;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TrackController {
    private final MusicService musicService;

    public TrackController(MusicService musicService) {
        this.musicService = musicService;
    }

    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.noContent(exchange, 204);
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.error(exchange, 405, "Only GET is supported for /api/tracks");
            return;
        }

        URI uri = exchange.getRequestURI();
        String moodParam = queryValue(uri, "mood");
        String genreParam = queryValue(uri, "genre");

        MoodType mood = null;
        if (moodParam != null && !moodParam.isBlank() && !"ANY".equalsIgnoreCase(moodParam)) {
            try {
                mood = MoodType.fromString(moodParam);
            } catch (IllegalArgumentException ex) {
                HttpResponses.error(exchange, 400, ex.getMessage());
                return;
            }
        }

        List<Track> tracks;
        if (mood != null && genreParam != null && !genreParam.isBlank() && !"ANY".equalsIgnoreCase(genreParam)) {
            tracks = musicService.filter(genreParam, mood, 100);
        } else if (mood != null) {
            tracks = musicService.getByMood(mood);
        } else if (genreParam != null && !genreParam.isBlank() && !"ANY".equalsIgnoreCase(genreParam)) {
            tracks = musicService.getByGenre(genreParam);
        } else {
            tracks = musicService.getAll();
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tracks", tracks.stream().map(Track::toMap).collect(Collectors.toList()));
        payload.put("genres", musicService.getGenres());
        payload.put("moods", musicService.getMoodLabels());
        payload.put("totalTracks", tracks.size());
        HttpResponses.json(exchange, 200, payload);
    }

    private String queryValue(URI uri, String key) {
        if (uri.getQuery() == null) {
            return null;
        }
        for (String part : uri.getQuery().split("&")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2 && pair[0].equalsIgnoreCase(key)) {
                return java.net.URLDecoder.decode(pair[1], java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}

