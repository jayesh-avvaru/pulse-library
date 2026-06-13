package com.pulselibrary.service;

import com.pulselibrary.dto.SongDTO;
import com.pulselibrary.util.ITunesJsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ITunesService {
    private static final String BASE_URL = "https://itunes.apple.com/search";
    private static final Map<String, String> MOOD_TERMS = new LinkedHashMap<>();

    static {
        MOOD_TERMS.put("happy", "upbeat pop hits");
        MOOD_TERMS.put("sad", "melancholic acoustic");
        MOOD_TERMS.put("energetic", "workout pump up");
        MOOD_TERMS.put("chill", "lo-fi chill beats");
        MOOD_TERMS.put("romantic", "romantic songs");
        MOOD_TERMS.put("focus", "study focus instrumental");
        MOOD_TERMS.put("uplift", "upbeat pop hits");
        MOOD_TERMS.put("energy", "workout pump up");
        MOOD_TERMS.put("late_night", "late night vibes");
        MOOD_TERMS.put("workout", "workout pump up");
    }

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(12))
        .build();

    public List<SongDTO> search(String query, int limit) throws IOException, InterruptedException {
        return fetch(query, Math.max(1, Math.min(limit, 50)));
    }

    public List<SongDTO> byGenre(String genre, int limit) throws IOException, InterruptedException {
        return fetch(genre + " music", Math.max(1, Math.min(limit, 50)));
    }

    public List<SongDTO> playlistByMood(String mood, int limit) throws IOException, InterruptedException {
        String key = mood == null ? "chill" : mood.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
        String term = MOOD_TERMS.getOrDefault(key, mood == null ? "chill playlist" : mood);
        return fetch(term, Math.max(1, Math.min(limit, 25)));
    }

    public List<SongDTO> recommendByArtist(String artist, int limit) throws IOException, InterruptedException {
        return fetch(artist, Math.max(1, Math.min(limit, 25)));
    }

    public List<SongDTO> trending(int limit) throws IOException, InterruptedException {
        return fetch("top hits 2025", Math.max(1, Math.min(limit, 50)));
    }

    public List<String> presetGenres() {
        return List.of("Pop", "Hip-Hop", "Rock", "Jazz", "Classical", "R&B", "Electronic", "Indie");
    }

    private List<SongDTO> fetch(String term, int limit) throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(term, StandardCharsets.UTF_8);
        String url = BASE_URL + "?term=" + encoded + "&entity=song&limit=" + limit + "&country=IN";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(15))
            .header("Accept", "application/json")
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("iTunes API returned status " + response.statusCode());
        }
        return ITunesJsonParser.parseResults(response.body());
    }
}
