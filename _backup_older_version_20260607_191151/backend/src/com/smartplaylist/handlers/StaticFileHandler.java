package com.smartplaylist.handlers;

import com.smartplaylist.util.HttpResponses;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class StaticFileHandler implements HttpHandler {
    private static final Map<String, String> CONTENT_TYPES = Map.of(
        "html", "text/html; charset=utf-8",
        "css", "text/css; charset=utf-8",
        "js", "application/javascript; charset=utf-8"
    );

    private final Path frontendDir;

    public StaticFileHandler(Path frontendDir) {
        this.frontendDir = frontendDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpResponses.noContent(exchange, 204);
            return;
        }

        if (
            !"GET".equalsIgnoreCase(exchange.getRequestMethod()) &&
            !"HEAD".equalsIgnoreCase(exchange.getRequestMethod())
        ) {
            HttpResponses.error(exchange, 405, "Static assets only support GET requests.");
            return;
        }

        String requestPath = exchange.getRequestURI().getPath();
        String relativePath = requestPath == null || requestPath.equals("/") ? "index.html" : requestPath.substring(1);
        Path target = frontendDir.resolve(relativePath).normalize();

        if (!target.startsWith(frontendDir) || Files.isDirectory(target) || !Files.exists(target)) {
            target = frontendDir.resolve("index.html").normalize();
        }

        byte[] bytes = Files.readAllBytes(target);
        HttpResponses.bytes(exchange, 200, contentType(target), bytes);
    }

    private String contentType(Path target) {
        String fileName = target.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return "application/octet-stream";
        }

        String extension = fileName.substring(dotIndex + 1).toLowerCase();
        return CONTENT_TYPES.getOrDefault(extension, "application/octet-stream");
    }
}
