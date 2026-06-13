package com.pulselibrary.service;

import com.pulselibrary.model.MoodType;
import com.pulselibrary.model.Track;
import java.util.List;

public interface MusicService {
    List<Track> getAll();

    List<Track> getByMood(MoodType mood);

    List<Track> getByGenre(String genre);

    List<Track> filter(String genre, MoodType mood, int limit);

    List<String> getGenres();

    List<String> getMoodLabels();
}

