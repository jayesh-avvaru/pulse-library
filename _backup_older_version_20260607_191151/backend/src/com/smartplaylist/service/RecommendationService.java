package com.smartplaylist.service;

import com.smartplaylist.exception.ValidationException;
import com.smartplaylist.model.Playlist;
import com.smartplaylist.model.RecommendationCriteria;
import com.smartplaylist.model.Song;
import com.smartplaylist.model.UserProfile;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public final class RecommendationService {
    private final MusicLibrary musicLibrary;
    private final ExecutorService workers =
        Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors()));
    private final Map<String, Playlist> generatedPlaylists = new ConcurrentHashMap<>();
    private final AtomicInteger totalGenerated = new AtomicInteger();

    public RecommendationService(MusicLibrary musicLibrary) {
        this.musicLibrary = musicLibrary;
    }

    public CompletableFuture<Playlist> recommendAsync(UserProfile user, RecommendationCriteria criteria) {
        return CompletableFuture.supplyAsync(() -> recommend(user, criteria), workers);
    }

    public Playlist generatedPlaylist(String playlistId) {
        Playlist playlist = generatedPlaylists.get(playlistId);
        if (playlist == null) {
            throw new ValidationException("Generate a playlist before saving it.");
        }
        return playlist;
    }

    public int totalGenerated() {
        return totalGenerated.get();
    }

    public void shutdown() {
        workers.shutdown();
    }

    private Playlist recommend(UserProfile user, RecommendationCriteria criteria) {
        if (user == null) {
            throw new ValidationException("A valid user session is required.");
        }

        int limit = clamp(criteria.getLimit(), 4, 10);
        List<Song> candidates = new ArrayList<>(musicLibrary.search(criteria.getGenre(), criteria.getMood()));

        candidates.sort(
            Comparator
                .comparingInt((Song song) -> score(song, user, criteria))
                .reversed()
                .thenComparing(Comparator.comparingInt(Song::getPopularity).reversed())
                .thenComparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER)
        );

        List<Song> chosenSongs = new ArrayList<>();
        Set<String> seenSongs = new LinkedHashSet<>();
        for (Song song : candidates) {
            if (seenSongs.add(song.getId())) {
                chosenSongs.add(song);
            }
            if (chosenSongs.size() == limit) {
                break;
            }
        }

        if (chosenSongs.isEmpty()) {
            throw new ValidationException("No songs matched the selected filters.");
        }

        String genre = normalizeChoice(criteria.getGenre());
        String mood = normalizeChoice(criteria.getMood());
        String name = buildName(user, genre, mood);
        String summary = "A curated mix tuned to " + mood.toLowerCase() + " energy and " + genre.toLowerCase() + " textures.";
        String accent = chosenSongs.get(0).getAccent();

        Playlist playlist = new Playlist(
            UUID.randomUUID().toString(),
            name,
            user.getUsername(),
            genre,
            mood,
            Instant.now().toString(),
            summary,
            accent,
            chosenSongs
        );

        generatedPlaylists.put(playlist.getId(), playlist);
        totalGenerated.incrementAndGet();
        user.rememberPlaylist(name);
        return playlist;
    }

    private int score(Song song, UserProfile user, RecommendationCriteria criteria) {
        int score = song.getPopularity();

        if (!"Any".equalsIgnoreCase(criteria.getGenre()) && song.getGenre().equalsIgnoreCase(criteria.getGenre())) {
            score += 22;
        }
        if (song.matchesMood(criteria.getMood())) {
            score += 25;
        }
        if (containsIgnoreCase(user.getFavoriteGenres(), song.getGenre())) {
            score += 14;
        }
        for (String mood : user.getFavoriteMoods()) {
            if (song.matchesMood(mood)) {
                score += 10;
                break;
            }
        }

        int targetEnergy = desiredEnergy(criteria.getMood());
        score += Math.max(0, 10 - Math.abs(song.getEnergy() - targetEnergy));
        return score;
    }

    private int desiredEnergy(String mood) {
        if (mood == null) {
            return 5;
        }

        return switch (mood.trim().toLowerCase()) {
            case "focus" -> 4;
            case "chill" -> 4;
            case "late night" -> 3;
            case "uplift" -> 7;
            case "energy" -> 8;
            case "workout" -> 9;
            default -> 5;
        };
    }

    private String buildName(UserProfile user, String genre, String mood) {
        if ("Any".equalsIgnoreCase(genre) && "Any".equalsIgnoreCase(mood)) {
            return user.getDisplayName() + " Essentials";
        }
        if ("Any".equalsIgnoreCase(genre)) {
            return mood + " Studio Mix";
        }
        if ("Any".equalsIgnoreCase(mood)) {
            return genre + " Select";
        }
        return mood + " " + genre + " Mix";
    }

    private boolean containsIgnoreCase(List<String> values, String target) {
        return values.stream().anyMatch(value -> value.equalsIgnoreCase(target));
    }

    private String normalizeChoice(String value) {
        if (value == null || value.isBlank()) {
            return "Any";
        }
        return value.trim();
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
