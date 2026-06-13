package com.pulselibrary.controller;

import com.pulselibrary.service.PlaylistService;
import com.pulselibrary.util.HttpResponses;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public final class StatsController {
    private final PlaylistService playlistService;

    public StatsController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    public void handleMoodDistribution(HttpExchange exchange) throws IOException {
        if (preflight(exchange)) {
            return;
        }
        HttpResponses.json(exchange, 200, playlistService.libraryMoodDistribution());
    }

    public void handleGenreCount(HttpExchange exchange) throws IOException {
        if (preflight(exchange)) {
            return;
        }
        HttpResponses.json(exchange, 200, playlistService.genreCounts());
    }

    public void handleDurationDistribution(HttpExchange exchange) throws IOException {
        if (preflight(exchange)) {
            return;
        }
        HttpResponses.json(exchange, 200, playlistService.durationDistribution());
    }

    public void handleOverview(HttpExchange exchange) throws IOException {
        if (preflight(exchange)) {
            return;
        }
        HttpResponses.json(exchange, 200, playlistService.overviewStats());
    }

    private boolean preflight(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.noContent(exchange, 204);
            return true;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.error(exchange, 405, "Only GET is supported");
            return true;
        }
        return false;
    }
}

