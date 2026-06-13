package com.pulselibrary;

import com.pulselibrary.controller.MusicController;
import com.pulselibrary.controller.PlaylistController;
import com.pulselibrary.controller.StatsController;
import com.pulselibrary.controller.TrackController;
import com.pulselibrary.handlers.StaticFileHandler;
import com.pulselibrary.repository.PlaylistRepository;
import com.pulselibrary.repository.TrackRepository;
import com.pulselibrary.service.ITunesService;
import com.pulselibrary.service.MusicService;
import com.pulselibrary.service.PlaylistService;
import com.pulselibrary.service.impl.MusicServiceImpl;
import com.pulselibrary.service.impl.PlaylistServiceImpl;
import com.pulselibrary.util.HttpResponses;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public final class Main {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        TrackRepository trackRepository = new TrackRepository();
        PlaylistRepository playlistRepository = new PlaylistRepository();
        MusicService musicService = new MusicServiceImpl(trackRepository);
        PlaylistService playlistService = new PlaylistServiceImpl(musicService, trackRepository, playlistRepository);
        ITunesService iTunesService = new ITunesService();

        TrackController trackController = new TrackController(musicService);
        PlaylistController playlistController = new PlaylistController(playlistService);
        StatsController statsController = new StatsController(playlistService);
        MusicController musicController = new MusicController(iTunesService);

        Path frontendDir = resolveFrontendDir();
        System.out.println("Serving frontend from: " + frontendDir.toAbsolutePath());

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
        server.setExecutor(Executors.newFixedThreadPool(12));

        server.createContext("/api/search", musicController::handleSearch);
        server.createContext("/api/genre", musicController::handleGenre);
        server.createContext("/api/playlist", musicController::handlePlaylist);
        server.createContext("/api/recommend", musicController::handleRecommend);
        server.createContext("/api/trending", musicController::handleTrending);
        server.createContext("/api/genres", musicController::handleGenres);

        server.createContext("/api/tracks", trackController::handle);
        server.createContext("/api/playlist/generate", playlistController::handleGenerate);
        server.createContext("/api/playlist/save", playlistController::handleSaveFromBody);
        server.createContext("/api/playlist/saved", playlistController::handleSaved);
        server.createContext(
            "/api/playlist/delete/",
            exchange -> {
                String path = exchange.getRequestURI().getPath();
                String prefix = "/api/playlist/delete/";
                String id = path.startsWith(prefix) ? path.substring(prefix.length()) : "";
                playlistController.handleDelete(exchange, id);
            }
        );
        server.createContext("/api/stats/mood-distribution", statsController::handleMoodDistribution);
        server.createContext("/api/stats/genre-count", statsController::handleGenreCount);
        server.createContext("/api/stats/duration-distribution", statsController::handleDurationDistribution);
        server.createContext("/api/stats/overview", statsController::handleOverview);
        server.createContext("/api/health", exchange -> {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpResponses.noContent(exchange, 204);
                return;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("status", "ok");
            payload.put("app", "Pulse Library");
            payload.put("runtime", System.getProperty("java.version"));
            HttpResponses.json(exchange, 200, payload);
        });
        server.createContext("/", new StaticFileHandler(frontendDir));

        server.start();
        System.out.println("Pulse Library running at http://127.0.0.1:" + PORT);
    }

    private static Path resolveFrontendDir() throws IOException {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path[] candidates = {
            cwd.resolve("frontend"),
            cwd.resolve("../frontend"),
            cwd.getParent() != null ? cwd.getParent().resolve("frontend") : cwd,
        };

        for (Path candidate : candidates) {
            Path normalized = candidate.normalize().toAbsolutePath();
            if (Files.isDirectory(normalized) && Files.exists(normalized.resolve("index.html"))) {
                return normalized;
            }
        }

        throw new IOException("Could not find frontend folder. Working directory was: " + cwd);
    }
}
