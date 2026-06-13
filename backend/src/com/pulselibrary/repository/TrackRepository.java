package com.pulselibrary.repository;

import com.pulselibrary.model.MoodType;
import com.pulselibrary.model.Track;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class TrackRepository {
    private static final Map<String, String> PREVIEW_URLS = Map.of(
        "Pop",
        "/previews/pop.mp3",
        "Electronic",
        "/previews/electronic.mp3",
        "Ambient",
        "/previews/ambient.mp3",
        "Indie",
        "/previews/indie.mp3",
        "R&B",
        "/previews/rnb.mp3",
        "Hip-Hop",
        "/previews/hiphop.mp3",
        "Rock",
        "/previews/rock.mp3"
    );
    private static final Map<String, String> PREVIEW_TRACKS = Map.of(
        "Pop",
        "Pop Preview",
        "Electronic",
        "Electronic Preview",
        "Ambient",
        "Ambient Preview",
        "Indie",
        "Indie Preview",
        "R&B",
        "R&B Preview",
        "Hip-Hop",
        "Hip-Hop Preview",
        "Rock",
        "Rock Preview"
    );
    private static final Map<String, String> PREVIEW_ARTISTS = Map.of(
        "Pop",
        "Pulse Library",
        "Electronic",
        "Pulse Library",
        "Ambient",
        "Pulse Library",
        "Indie",
        "Pulse Library",
        "R&B",
        "Pulse Library",
        "Hip-Hop",
        "Pulse Library",
        "Rock",
        "Pulse Library"
    );

    private final Map<String, Track> tracksById = new LinkedHashMap<>();

    public TrackRepository() {
        seedTracks();
    }

    public List<Track> findAll() {
        return List.copyOf(tracksById.values());
    }

    public Optional<Track> findById(String id) {
        return Optional.ofNullable(tracksById.get(id));
    }

    public List<Track> findByMood(MoodType mood) {
        if (mood == null) {
            return findAll();
        }
        return tracksById.values().stream().filter(track -> track.hasMood(mood)).collect(Collectors.toList());
    }

    public List<Track> findByGenre(String genre) {
        if (genre == null || genre.isBlank() || "ANY".equalsIgnoreCase(genre.trim())) {
            return findAll();
        }
        String target = genre.trim().toLowerCase();
        return tracksById.values().stream()
            .filter(track -> track.getGenre().equalsIgnoreCase(target))
            .collect(Collectors.toList());
    }

    public void save(Track track) {
        tracksById.put(track.getId(), track);
    }

    private void seedTracks() {
        add(track("S01", "Velvet Skyline", "Nova Bloom", "Pop", MoodType.UPLIFT, MoodType.ENERGY, 202, 88, 8, "#ff7b7b"));
        add(track("S02", "Static Hearts", "Orion East", "Pop", MoodType.CHILL, MoodType.UPLIFT, 214, 84, 6, "#ff9d6c"));
        add(track("S03", "Night Drive", "Aurora Youth", "Electronic", MoodType.LATE_NIGHT, MoodType.CHILL, 236, 91, 5, "#7a6bff"));
        add(track("S04", "Golden Hour Loop", "Mira Lane", "Ambient", MoodType.FOCUS, MoodType.CHILL, 248, 78, 3, "#ffb55f"));
        add(track("S05", "Glass Oceans", "Polar Kids", "Indie", MoodType.CHILL, MoodType.LATE_NIGHT, 225, 81, 4, "#4e8df5"));
        add(track("S06", "Neon Season", "Atlas North", "Electronic", MoodType.ENERGY, MoodType.WORKOUT, 198, 86, 9, "#ff5f8a"));
        add(track("S07", "Moonlit Signal", "Iris Vale", "R&B", MoodType.LATE_NIGHT, MoodType.CHILL, 221, 79, 4, "#7366f8"));
        add(track("S08", "Afterglow Theory", "Felix Rowe", "Indie", MoodType.FOCUS, MoodType.UPLIFT, 217, 82, 5, "#7fd1b9"));
        add(track("S09", "Skyline Verse", "Tenfold Club", "Hip-Hop", MoodType.ENERGY, MoodType.UPLIFT, 210, 87, 8, "#ff6b44"));
        add(track("S10", "Quiet Neon", "Sora Field", "Ambient", MoodType.FOCUS, MoodType.LATE_NIGHT, 243, 77, 2, "#4f6ef7"));
        add(track("S11", "Satin Echo", "June Halo", "R&B", MoodType.CHILL, MoodType.UPLIFT, 216, 80, 5, "#ff6fa6"));
        add(track("S12", "Pulse Archive", "Kinetic Sun", "Electronic", MoodType.WORKOUT, MoodType.ENERGY, 191, 89, 10, "#ef476f"));
        add(track("S13", "Honey District", "Coastline Youth", "Pop", MoodType.CHILL, MoodType.UPLIFT, 206, 83, 6, "#ffb347"));
        add(track("S14", "Mirror Run", "Silver Axis", "Electronic", MoodType.FOCUS, MoodType.ENERGY, 204, 85, 7, "#7f5af0"));
        add(track("S15", "Bloom Again", "Luma Park", "Pop", MoodType.UPLIFT, MoodType.FOCUS, 212, 88, 6, "#ff5c8d"));
        add(track("S16", "Midnight Paper", "West Harbor", "Indie", MoodType.LATE_NIGHT, MoodType.FOCUS, 232, 76, 3, "#6e85ff"));
        add(track("S17", "Crimson Tape", "North Avenue", "Hip-Hop", MoodType.WORKOUT, MoodType.ENERGY, 208, 84, 9, "#ff6740"));
        add(track("S18", "Soft Static", "Blue Marble", "Ambient", MoodType.CHILL, MoodType.FOCUS, 251, 75, 2, "#8ec5ff"));
        add(track("S19", "Blue Velvet Rain", "Solstice Avenue", "R&B", MoodType.LATE_NIGHT, MoodType.UPLIFT, 223, 81, 5, "#8b5cf6"));
        add(track("S20", "Satellite Summer", "Palm Arcade", "Pop", MoodType.ENERGY, MoodType.CHILL, 199, 86, 7, "#ff7f50"));
        add(track("S21", "Crystal Frequency", "Echo Prism", "Electronic", MoodType.FOCUS, MoodType.ENERGY, 207, 84, 7, "#5b8cff"));
        add(track("S22", "Sunset Boulevard", "Maya Rivers", "Pop", MoodType.UPLIFT, MoodType.CHILL, 218, 85, 6, "#ff8fab"));
        add(track("S23", "Deep Current", "Submarine Blue", "Ambient", MoodType.LATE_NIGHT, MoodType.CHILL, 267, 74, 2, "#3d5a80"));
        add(track("S24", "Thunder Lane", "Voltage Crew", "Hip-Hop", MoodType.WORKOUT, MoodType.ENERGY, 195, 90, 10, "#e63946"));
        add(track("S25", "Paper Lanterns", "Hana West", "Indie", MoodType.UPLIFT, MoodType.FOCUS, 229, 80, 5, "#a8dadc"));
        add(track("S26", "Electric Dreams", "Synth Collective", "Electronic", MoodType.LATE_NIGHT, MoodType.ENERGY, 201, 87, 8, "#9d4edd"));
        add(track("S27", "Morning Coffee", "Jazz & Co", "R&B", MoodType.CHILL, MoodType.FOCUS, 240, 78, 4, "#c77dff"));
        add(track("S28", "Rooftop Anthem", "City Lights", "Pop", MoodType.ENERGY, MoodType.UPLIFT, 203, 89, 9, "#ff006e"));
        add(track("S29", "Whisper Wind", "Forest Echo", "Ambient", MoodType.FOCUS, MoodType.CHILL, 255, 76, 3, "#588157"));
        add(track("S30", "Neon Pulse", "Digital Hearts", "Electronic", MoodType.WORKOUT, MoodType.ENERGY, 189, 91, 10, "#00f5d4"));
        add(track("S31", "Stardust Waltz", "Luna Orchestra", "Indie", MoodType.LATE_NIGHT, MoodType.UPLIFT, 234, 79, 4, "#bdb2ff"));
        add(track("S32", "Fire Starter", "Blaze Unit", "Hip-Hop", MoodType.WORKOUT, MoodType.ENERGY, 192, 88, 9, "#ff5400"));
        add(track("S33", "Ocean Breeze", "Coastal Wave", "Pop", MoodType.CHILL, MoodType.UPLIFT, 211, 82, 5, "#48cae4"));
        add(track("S34", "Midnight Runner", "Night Shift", "Electronic", MoodType.LATE_NIGHT, MoodType.FOCUS, 228, 83, 6, "#7209b7"));
        add(track("S35", "Golden Mile", "Highway Kings", "Rock", MoodType.ENERGY, MoodType.WORKOUT, 205, 86, 8, "#d00000"));
        add(track("S36", "Velvet Room", "Smooth Operators", "R&B", MoodType.LATE_NIGHT, MoodType.CHILL, 226, 81, 4, "#7b2cbf"));
        add(track("S37", "Pixel Paradise", "Retro Wave", "Electronic", MoodType.UPLIFT, MoodType.ENERGY, 196, 88, 8, "#00bbf9"));
        add(track("S38", "Autumn Leaves", "Seasons Band", "Indie", MoodType.CHILL, MoodType.FOCUS, 238, 77, 3, "#bc6c25"));
        add(track("S39", "Power Hour", "Gym Beats", "Hip-Hop", MoodType.WORKOUT, MoodType.ENERGY, 187, 92, 10, "#ff4d6d"));
        add(track("S40", "Starlight Serenade", "Celestial Duo", "Pop", MoodType.LATE_NIGHT, MoodType.UPLIFT, 220, 84, 6, "#ffd60a"));
    }

    private void add(Track track) {
        tracksById.put(track.getId(), track);
    }

    private Track track(
        String id,
        String title,
        String artist,
        String genre,
        MoodType mood1,
        MoodType mood2,
        int duration,
        int popularity,
        int energy,
        String accent
    ) {
        return new Track.Builder()
            .id(id)
            .title(title)
            .artist(artist)
            .genre(genre)
            .mood(mood1)
            .mood(mood2)
            .duration(duration)
            .popularity(popularity)
            .energy(energy)
            .accent(accent)
            .previewUrl(PREVIEW_URLS.getOrDefault(genre, PREVIEW_URLS.get("Pop")))
            .previewSource(
                PREVIEW_TRACKS.getOrDefault(genre, PREVIEW_TRACKS.get("Pop")),
                PREVIEW_ARTISTS.getOrDefault(genre, PREVIEW_ARTISTS.get("Pop"))
            )
            .build();
    }
}
