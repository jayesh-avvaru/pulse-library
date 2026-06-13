package com.pulselibrary.repository;

import com.pulselibrary.model.Playlist;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PlaylistRepository {
    private final Map<String, Playlist> savedById = new LinkedHashMap<>();

    public void save(Playlist playlist) {
        savedById.put(playlist.getId(), playlist);
    }

    public List<Playlist> findAllSaved() {
        List<Playlist> playlists = new ArrayList<>(savedById.values());
        Collections.reverse(playlists);
        return playlists;
    }

    public Optional<Playlist> findById(String id) {
        return Optional.ofNullable(savedById.get(id));
    }

    public boolean delete(String id) {
        return savedById.remove(id) != null;
    }
}

