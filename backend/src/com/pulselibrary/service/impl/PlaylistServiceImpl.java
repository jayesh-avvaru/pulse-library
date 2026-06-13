package com.pulselibrary.service.impl;

import com.pulselibrary.model.GeneratedPlaylist;
import com.pulselibrary.model.MoodType;
import com.pulselibrary.model.Playlist;
import com.pulselibrary.model.Track;
import com.pulselibrary.repository.PlaylistRepository;
import com.pulselibrary.repository.TrackRepository;
import com.pulselibrary.service.MusicService;
import com.pulselibrary.service.PlaylistService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlaylistServiceImpl implements PlaylistService {
    private final MusicService musicService;
    private final TrackRepository trackRepository;
    private final PlaylistRepository playlistRepository;
    private final Map<String, GeneratedPlaylist> generatedCache = new ConcurrentHashMap<>();

    public PlaylistServiceImpl(
        MusicService musicService,
        TrackRepository trackRepository,
        PlaylistRepository playlistRepository
    ) {
        this.musicService = musicService;
        this.trackRepository = trackRepository;
        this.playlistRepository = playlistRepository;
    }

    @Override
    public GeneratedPlaylist generate(String genre, MoodType mood, int trackCount) {
        int limit = Math.max(4, Math.min(10, trackCount <= 0 ? 6 : trackCount));
        List<Track> tracks = pickTracks(genre, mood, limit);
        String genreLabel = genre == null || genre.isBlank() || "ANY".equalsIgnoreCase(genre) ? "Any" : genre.trim();
        String moodLabel = mood == null ? "Any" : mood.getLabel();
        String title = buildTitle(genreLabel, moodLabel);
        GeneratedPlaylist generated = new GeneratedPlaylist(
            UUID.randomUUID().toString(),
            title,
            tracks,
            genreLabel,
            moodLabel,
            Instant.now()
        );
        generatedCache.put(generated.getId(), generated);
        return generated;
    }

    @Override
    public void save(Playlist playlist) {
        if (playlist instanceof GeneratedPlaylist generatedPlaylist) {
            playlistRepository.save(generatedPlaylist);
            generatedCache.put(generatedPlaylist.getId(), generatedPlaylist);
            return;
        }
        generatedCache.getOrDefault(playlist.getId(), null);
        playlistRepository.save(playlist);
    }

    @Override
    public void saveById(String playlistId) {
        GeneratedPlaylist generated = generatedCache.get(playlistId);
        if (generated == null) {
            throw new IllegalArgumentException("Playlist not found. Generate it first.");
        }
        playlistRepository.save(generated);
    }

    @Override
    public List<Playlist> getAllSaved() {
        return playlistRepository.findAllSaved();
    }

    @Override
    public boolean delete(String playlistId) {
        return playlistRepository.delete(playlistId);
    }

    @Override
    public Map<String, Integer> moodDistributionForPlaylist(Playlist playlist) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (MoodType mood : MoodType.values()) {
            counts.put(mood.getLabel(), 0);
        }
        for (Track track : playlist.getTracks()) {
            for (MoodType mood : track.getMoods()) {
                counts.merge(mood.getLabel(), 1, Integer::sum);
            }
        }
        return counts;
    }

    @Override
    public Map<String, Integer> genreCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Track track : trackRepository.findAll()) {
            counts.merge(track.getGenre(), 1, Integer::sum);
        }
        return counts;
    }

    @Override
    public Map<String, Integer> durationDistribution() {
        Map<String, Integer> buckets = new LinkedHashMap<>();
        buckets.put("2-3min", 0);
        buckets.put("3-4min", 0);
        buckets.put("4-5min", 0);
        for (Track track : trackRepository.findAll()) {
            int minutes = track.getDurationSeconds() / 60;
            if (minutes < 3) {
                buckets.merge("2-3min", 1, Integer::sum);
            } else if (minutes < 4) {
                buckets.merge("3-4min", 1, Integer::sum);
            } else {
                buckets.merge("4-5min", 1, Integer::sum);
            }
        }
        return buckets;
    }

    @Override
    public Map<String, Integer> libraryMoodDistribution() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (MoodType mood : MoodType.values()) {
            counts.put(mood.getLabel(), 0);
        }
        for (Track track : trackRepository.findAll()) {
            for (MoodType mood : track.getMoods()) {
                counts.merge(mood.getLabel(), 1, Integer::sum);
            }
        }
        return counts;
    }

    @Override
    public Map<String, Object> overviewStats() {
        List<Track> tracks = trackRepository.findAll();
        int totalDuration = tracks.stream().mapToInt(Track::getDurationSeconds).sum();
        int avg = tracks.isEmpty() ? 0 : totalDuration / tracks.size();
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("totalTracks", tracks.size());
        overview.put("totalGenres", musicService.getGenres().size());
        overview.put("totalMoods", MoodType.values().length);
        overview.put("avgDuration", avg);
        overview.put("avgDurationLabel", String.format("%d:%02d", avg / 60, avg % 60));
        return overview;
    }

    private List<Track> pickTracks(String genre, MoodType mood, int limit) {
        boolean anyGenre = genre == null || genre.isBlank() || "ANY".equalsIgnoreCase(genre);
        boolean anyMood = mood == null;

        if (anyGenre && anyMood) {
            List<Track> all = new ArrayList<>(trackRepository.findAll());
            Collections.shuffle(all);
            return all.subList(0, Math.min(limit, all.size()));
        }

        return musicService.filter(anyGenre ? "ANY" : genre, mood, limit);
    }

    private String buildTitle(String genre, String mood) {
        if ("Any".equalsIgnoreCase(genre) && "Any".equalsIgnoreCase(mood)) {
            return "Pulse Essentials";
        }
        if ("Any".equalsIgnoreCase(genre)) {
            return mood + " Studio Mix";
        }
        if ("Any".equalsIgnoreCase(mood)) {
            return genre + " Select";
        }
        return mood + " " + genre + " Mix";
    }
}
