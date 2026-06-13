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
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
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

    // ── CORS helper ───────────────────────────────────────────
    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Accept, Authorization");
    }

    // Wraps any handler with CORS + OPTIONS preflight support
    private static HttpHandler cors(HttpHandler handler) {
        return exchange -> {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            handler.handle(exchange);
        };
    }

    // ── Main ──────────────────────────────────────────────────
    public static void main(String[] args) throws IOException {
        TrackRepository    trackRepository    = new TrackRepository();
        PlaylistRepository playlistRepository = new PlaylistRepository();
        MusicService       musicService       = new MusicServiceImpl(trackRepository);
        PlaylistService    playlistService    = new PlaylistServiceImpl(musicService, trackRepository, playlistRepository);
        ITunesService      iTunesService      = new ITunesService();

        TrackController    trackController    = new TrackController(musicService);
        PlaylistController playlistController = new PlaylistController(playlistService);
        StatsController    statsController    = new StatsController(playlistService);
        MusicController    musicController    = new MusicController(iTunesService);

        Path frontendDir = resolveFrontendDir();
        System.out.println("Serving frontend from: " + frontendDir.toAbsolutePath());

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.setExecutor(Executors.newFixedThreadPool(12));

        // ── iTunes / Music endpoints ───────────────────────────
        server.createContext("/api/search",    cors(musicController::handleSearch));
        server.createContext("/api/genre",     cors(musicController::handleGenre));
        server.createContext("/api/playlist",  cors(musicController::handlePlaylist));
        server.createContext("/api/recommend", cors(musicController::handleRecommend));
        server.createContext("/api/trending",  cors(musicController::handleTrending));
        server.createContext("/api/genres",    cors(musicController::handleGenres));

        // ── Track endpoints ───────────────────────────────────
        server.createContext("/api/tracks", cors(trackController::handle));

        // ── Playlist endpoints ────────────────────────────────
        server.createContext("/api/playlist/generate", cors(playlistController::handleGenerate));
        server.createContext("/api/playlist/save",     cors(playlistController::handleSaveFromBody));
        server.createContext("/api/playlist/saved",    cors(playlistController::handleSaved));
        server.createContext("/api/playlist/delete/",  cors(exchange -> {
            String path   = exchange.getRequestURI().getPath();
            String prefix = "/api/playlist/delete/";
            String id     = path.startsWith(prefix) ? path.substring(prefix.length()) : "";
            playlistController.handleDelete(exchange, id);
        }));

        // ── Stats endpoints ───────────────────────────────────
        server.createContext("/api/stats/mood-distribution",     cors(statsController::handleMoodDistribution));
        server.createContext("/api/stats/genre-count",           cors(statsController::handleGenreCount));
        server.createContext("/api/stats/duration-distribution", cors(statsController::handleDurationDistribution));
        server.createContext("/api/stats/overview",              cors(statsController::handleOverview));

        // ── Health check ──────────────────────────────────────
        server.createContext("/api/health", cors(exchange -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("status",  "ok");
            payload.put("app",     "Pulse Library");
            payload.put("runtime", System.getProperty("java.version"));
            HttpResponses.json(exchange, 200, payload);
        }));

        // ── Static frontend ───────────────────────────────────
        server.createContext("/", new StaticFileHandler(frontendDir));

        server.start();
        System.out.println("Pulse Library running at http://0.0.0.0:" + PORT);
    }

    // ── Frontend folder resolver ──────────────────────────────
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

        throw new IOException(
            "Could not find frontend folder. Working directory was: " + cwd
        );
    }
}