package com.pulselibrary.service;

import com.pulselibrary.model.GeneratedPlaylist;
import com.pulselibrary.model.MoodType;
import com.pulselibrary.model.Playlist;
import java.util.List;
import java.util.Map;

public interface PlaylistService {
    GeneratedPlaylist generate(String genre, MoodType mood, int trackCount);

    void save(Playlist playlist);

    void saveById(String playlistId);

    List<Playlist> getAllSaved();

    Map<String, Integer> libraryMoodDistribution();

    boolean delete(String playlistId);

    Map<String, Integer> moodDistributionForPlaylist(Playlist playlist);

    Map<String, Integer> genreCounts();

    Map<String, Integer> durationDistribution();

    Map<String, Object> overviewStats();
}

