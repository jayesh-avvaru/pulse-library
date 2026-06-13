package com.pulselibrary.service.impl;

import com.pulselibrary.model.MoodType;
import com.pulselibrary.model.Track;
import com.pulselibrary.repository.TrackRepository;
import com.pulselibrary.service.MusicService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class MusicServiceImpl implements MusicService {
    private final TrackRepository trackRepository;

    public MusicServiceImpl(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    @Override
    public List<Track> getAll() {
        return trackRepository.findAll();
    }

    @Override
    public List<Track> getByMood(MoodType mood) {
        return trackRepository.findByMood(mood);
    }

    @Override
    public List<Track> getByGenre(String genre) {
        return trackRepository.findByGenre(genre);
    }

    @Override
    public List<Track> filter(String genre, MoodType mood, int limit) {
        List<Track> candidates = new ArrayList<>(trackRepository.findByGenre(genre));
        if (mood != null) {
            candidates = candidates.stream().filter(track -> track.hasMood(mood)).collect(Collectors.toList());
        }

        if (candidates.isEmpty()) {
            candidates = new ArrayList<>(trackRepository.findAll());
        }

        candidates.sort(
            Comparator
                .comparingInt(Track::getPopularity)
                .reversed()
                .thenComparing(Track::getTitle, String.CASE_INSENSITIVE_ORDER)
        );

        Set<String> seen = new LinkedHashSet<>();
        List<Track> result = new ArrayList<>();
        for (Track track : candidates) {
            if (seen.add(track.getId())) {
                result.add(track);
            }
            if (result.size() >= Math.max(1, limit)) {
                break;
            }
        }
        return result;
    }

    @Override
    public List<String> getGenres() {
        return trackRepository.findAll().stream()
            .map(Track::getGenre)
            .distinct()
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getMoodLabels() {
        List<String> labels = new ArrayList<>();
        for (MoodType mood : MoodType.values()) {
            labels.add(mood.getLabel());
        }
        return labels;
    }
}
