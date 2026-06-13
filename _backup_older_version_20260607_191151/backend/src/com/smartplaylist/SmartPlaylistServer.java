package com.smartplaylist;

import com.smartplaylist.exception.ValidationException;
import com.smartplaylist.handlers.StaticFileHandler;
import com.smartplaylist.model.Playlist;
import com.smartplaylist.model.RecommendationCriteria;
import com.smartplaylist.model.UserProfile;
import com.smartplaylist.service.AuthService;
import com.smartplaylist.service.MusicLibrary;
import com.smartplaylist.service.PlaylistStore;
import com.smartplaylist.service.RecommendationService;
import com.smartplaylist.util.HttpResponses;
import com.smartplaylist.util.RequestData;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SmartPlaylistServer {
    private static final int PORT = 8080;

    private final AuthService authService = new AuthService();
    private final MusicLibrary musicLibrary = new MusicLibrary();
    private final RecommendationService recommendationService = new RecommendationService(musicLibrary);
    private final PlaylistStore playlistStore = new PlaylistStore();
    private final Path frontendDir = resolveFrontendDir();

    public static void main(String[] args) throws IOException {
        new SmartPlaylistServer().start();
    }

    private void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        ExecutorService httpExecutor =
            Executors.newFixedThreadPool(Math.max(6, Runtime.getRuntime().availableProcessors() * 2));

        server.setExecutor(httpExecutor);
        server.createContext("/api/health", api(this::handleHealth));
        server.createContext("/api/library", api(this::handleLibrary));
        server.createContext("/api/login", api(this::handleLogin));
        server.createContext("/api/recommendations", api(this::handleRecommendations));
        server.createContext("/api/playlists", api(this::handlePlaylists));
        server.createContext("/api/playlists/save", api(this::handleSavePlaylist));
        server.createContext("/api/insights", api(this::handleInsights));
        server.createContext("/", new StaticFileHandler(frontendDir));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            recommendationService.shutdown();
            httpExecutor.shutdown();
            server.stop(0);
        }));

        server.start();
        System.out.println("Smart Playlist Recommender running at http://127.0.0.1:" + PORT);
    }

    private HttpHandler api(ExchangeAction action) {
        return exchange -> {
            try {
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    HttpResponses.noContent(exchange, 204);
                    return;
                }
                action.handle(exchange);
            } catch (ValidationException error) {
                HttpResponses.error(exchange, 400, error.getMessage());
            } catch (CompletionException error) {
                Throwable cause = error.getCause();
                if (cause instanceof ValidationException validationError) {
                    HttpResponses.error(exchange, 400, validationError.getMessage());
                    return;
                }
                error.printStackTrace();
                HttpResponses.error(exchange, 500, "The Java backend could not process that request.");
            } catch (Exception error) {
                error.printStackTrace();
                HttpResponses.error(exchange, 500, "The Java backend could not process that request.");
            }
        };
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        if (!allowMethod(exchange, "GET")) {
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "ok");
        payload.put("app", "Smart Playlist Recommender");
        payload.put("runtime", System.getProperty("java.version"));
        HttpResponses.json(exchange, 200, payload);
    }

    private void handleLibrary(HttpExchange exchange) throws IOException {
        if (!allowMethod(exchange, "GET")) {
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("genres", musicLibrary.getGenres());
        payload.put("moods", musicLibrary.getMoods());
        payload.put(
            "featuredSongs",
            musicLibrary.getFeaturedSongs().stream().map(song -> song.toMap()).toList()
        );
        payload.put("totalSongs", musicLibrary.totalSongs());
        HttpResponses.json(exchange, 200, payload);
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        if (!allowMethod(exchange, "POST")) {
            return;
        }

        Map<String, String> form = RequestData.readForm(exchange);
        UserProfile user = authService.authenticate(
            RequestData.required(form, "username"),
            RequestData.required(form, "password")
        );

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", "Login successful");
        payload.put("user", user.toPublicMap());
        HttpResponses.json(exchange, 200, payload);
    }

    private void handleRecommendations(HttpExchange exchange) throws IOException {
        if (!allowMethod(exchange, "POST")) {
            return;
        }

        Map<String, String> form = RequestData.readForm(exchange);
        UserProfile user = authService.requireUser(RequestData.required(form, "username"));

        RecommendationCriteria criteria = new RecommendationCriteria(
            user.getUsername(),
            RequestData.optional(form, "genre", "Any"),
            RequestData.optional(form, "mood", "Any"),
            RequestData.intValue(form, "limit", 6)
        );

        Playlist playlist = recommendationService.recommendAsync(user, criteria).join();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", "Playlist generated");
        payload.put("playlist", playlist.toMap());
        HttpResponses.json(exchange, 200, payload);
    }

    private void handlePlaylists(HttpExchange exchange) throws IOException {
        if (!allowMethod(exchange, "GET")) {
            return;
        }

        Map<String, String> query = RequestData.query(exchange);
        UserProfile user = authService.requireUser(RequestData.required(query, "username"));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put(
            "playlists",
            playlistStore.listForUser(user.getUsername()).stream().map(playlist -> playlist.toMap()).toList()
        );
        HttpResponses.json(exchange, 200, payload);
    }

    private void handleSavePlaylist(HttpExchange exchange) throws IOException {
        if (!allowMethod(exchange, "POST")) {
            return;
        }

        Map<String, String> form = RequestData.readForm(exchange);
        UserProfile user = authService.requireUser(RequestData.required(form, "username"));
        Playlist playlist = recommendationService.generatedPlaylist(RequestData.required(form, "playlistId"));
        playlistStore.saveForUser(user.getUsername(), playlist);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", "Playlist saved to your shelf");
        payload.put("savedPlaylists", playlistStore.countForUser(user.getUsername()));
        HttpResponses.json(exchange, 200, payload);
    }

    private void handleInsights(HttpExchange exchange) throws IOException {
        if (!allowMethod(exchange, "GET")) {
            return;
        }

        Map<String, String> query = RequestData.query(exchange);
        UserProfile user = authService.requireUser(RequestData.required(query, "username"));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("displayName", user.getDisplayName());
        payload.put("favoriteGenres", user.getFavoriteGenres());
        payload.put("favoriteMoods", user.getFavoriteMoods());
        payload.put("history", user.getListeningHistory());
        payload.put("totalSongs", musicLibrary.totalSongs());
        payload.put("savedPlaylists", playlistStore.countForUser(user.getUsername()));
        payload.put("generatedPlaylists", recommendationService.totalGenerated());
        HttpResponses.json(exchange, 200, payload);
    }

    private boolean allowMethod(HttpExchange exchange, String allowedMethod) throws IOException {
        if (allowedMethod.equalsIgnoreCase(exchange.getRequestMethod())) {
            return true;
        }

        HttpResponses.error(exchange, 405, "Use " + allowedMethod + " for this endpoint.");
        return false;
    }

    private Path resolveFrontendDir() {
        Path sibling = Path.of("..", "frontend").toAbsolutePath().normalize();
        if (Files.isDirectory(sibling)) {
            return sibling;
        }

        Path local = Path.of("frontend").toAbsolutePath().normalize();
        if (Files.isDirectory(local)) {
            return local;
        }

        return sibling;
    }

    @FunctionalInterface
    private interface ExchangeAction {
        void handle(HttpExchange exchange) throws Exception;
    }
}
