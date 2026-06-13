package com.smartplaylist.service;

import com.smartplaylist.exception.ValidationException;
import com.smartplaylist.model.Song;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class MusicLibrary {
    private final Map<String, Song> songsById = new LinkedHashMap<>();
    private final Map<String, List<Song>> songsByGenre = new LinkedHashMap<>();
    private final Map<String, List<Song>> songsByMood = new LinkedHashMap<>();
    private final Map<String, String> genreLabels = new LinkedHashMap<>();
    private final Map<String, String> moodLabels = new LinkedHashMap<>();

    public MusicLibrary() {
        seedSongs();
    }

    public List<String> getGenres() {
        return List.copyOf(genreLabels.values());
    }

    public List<String> getMoods() {
        return List.copyOf(moodLabels.values());
    }

    public List<Song> getFeaturedSongs() {
        List<Song> allSongs = new ArrayList<>(songsById.values());
        return List.copyOf(allSongs.subList(0, Math.min(6, allSongs.size())));
    }

    public int totalSongs() {
        return songsById.size();
    }

    public List<Song> search(String genre, String mood) {
        boolean anyGenre = genre == null || genre.isBlank() || "Any".equalsIgnoreCase(genre);
        boolean anyMood = mood == null || mood.isBlank() || "Any".equalsIgnoreCase(mood);

        if (!anyGenre && !songsByGenre.containsKey(normalize(genre))) {
            throw new ValidationException("Unsupported genre. Choose from the available options.");
        }
        if (!anyMood && !songsByMood.containsKey(normalize(mood))) {
            throw new ValidationException("Unsupported mood. Choose from the available options.");
        }

        List<Song> baseSongs = anyGenre
            ? new ArrayList<>(songsById.values())
            : new ArrayList<>(songsByGenre.get(normalize(genre)));

        LinkedHashSet<Song> ordered = new LinkedHashSet<>();

        for (Song song : baseSongs) {
            if (song.matchesMood(mood)) {
                ordered.add(song);
            }
        }

        for (Song song : baseSongs) {
            ordered.add(song);
        }

        if (!anyMood) {
            for (Song song : songsByMood.get(normalize(mood))) {
                ordered.add(song);
            }
        }

        ordered.addAll(songsById.values());
        return new ArrayList<>(ordered);
    }

    private void seedSongs() {
        addSong(song("S01", "Velvet Skyline", "Nova Bloom", "Pop", List.of("Uplift", "Energy"), 202, 88, 8, "#ff7b7b"));
        addSong(song("S02", "Static Hearts", "Orion East", "Pop", List.of("Chill", "Uplift"), 214, 84, 6, "#ff9d6c"));
        addSong(song("S03", "Night Drive", "Aurora Youth", "Electronic", List.of("Late Night", "Chill"), 236, 91, 5, "#7a6bff"));
        addSong(song("S04", "Golden Hour Loop", "Mira Lane", "Ambient", List.of("Focus", "Chill"), 248, 78, 3, "#ffb55f"));
        addSong(song("S05", "Glass Oceans", "Polar Kids", "Indie", List.of("Chill", "Late Night"), 225, 81, 4, "#4e8df5"));
        addSong(song("S06", "Neon Season", "Atlas North", "Electronic", List.of("Energy", "Workout"), 198, 86, 9, "#ff5f8a"));
        addSong(song("S07", "Moonlit Signal", "Iris Vale", "R&B", List.of("Late Night", "Chill"), 221, 79, 4, "#7366f8"));
        addSong(song("S08", "Afterglow Theory", "Felix Rowe", "Indie", List.of("Focus", "Uplift"), 217, 82, 5, "#7fd1b9"));
        addSong(song("S09", "Skyline Verse", "Tenfold Club", "Hip-Hop", List.of("Energy", "Uplift"), 210, 87, 8, "#ff6b44"));
        addSong(song("S10", "Quiet Neon", "Sora Field", "Ambient", List.of("Focus", "Late Night"), 243, 77, 2, "#4f6ef7"));
        addSong(song("S11", "Satin Echo", "June Halo", "R&B", List.of("Chill", "Uplift"), 216, 80, 5, "#ff6fa6"));
        addSong(song("S12", "Pulse Archive", "Kinetic Sun", "Electronic", List.of("Workout", "Energy"), 191, 89, 10, "#ef476f"));
        addSong(song("S13", "Honey District", "Coastline Youth", "Pop", List.of("Chill", "Uplift"), 206, 83, 6, "#ffb347"));
        addSong(song("S14", "Mirror Run", "Silver Axis", "Electronic", List.of("Focus", "Energy"), 204, 85, 7, "#7f5af0"));
        addSong(song("S15", "Bloom Again", "Luma Park", "Pop", List.of("Uplift", "Focus"), 212, 88, 6, "#ff5c8d"));
        addSong(song("S16", "Midnight Paper", "West Harbor", "Indie", List.of("Late Night", "Focus"), 232, 76, 3, "#6e85ff"));
        addSong(song("S17", "Crimson Tape", "North Avenue", "Hip-Hop", List.of("Workout", "Energy"), 208, 84, 9, "#ff6740"));
        addSong(song("S18", "Soft Static", "Blue Marble", "Ambient", List.of("Chill", "Focus"), 251, 75, 2, "#8ec5ff"));
        addSong(song("S19", "Blue Velvet Rain", "Solstice Avenue", "R&B", List.of("Late Night", "Uplift"), 223, 81, 5, "#8b5cf6"));
        addSong(song("S20", "Satellite Summer", "Palm Arcade", "Pop", List.of("Energy", "Chill"), 199, 86, 7, "#ff7f50"));
    }

    private void addSong(Song song) {
        if (songsById.containsKey(song.getId())) {
            throw new IllegalStateException("Duplicate song id found: " + song.getId());
        }

        songsById.put(song.getId(), song);

        String genreKey = normalize(song.getGenre());
        genreLabels.putIfAbsent(genreKey, song.getGenre());
        songsByGenre.computeIfAbsent(genreKey, key -> new ArrayList<>()).add(song);

        for (String mood : song.getMoods()) {
            String moodKey = normalize(mood);
            moodLabels.putIfAbsent(moodKey, mood);
            songsByMood.computeIfAbsent(moodKey, key -> new ArrayList<>()).add(song);
        }
    }

    private Song song(
        String id,
        String title,
        String artist,
        String genre,
        List<String> moods,
        int durationSeconds,
        int popularity,
        int energy,
        String accent
    ) {
        return new Song(id, title, artist, genre, moods, durationSeconds, popularity, energy, accent);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
