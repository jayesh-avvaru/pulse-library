package com.smartplaylist.util;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpResponses {
    private HttpResponses() {
    }

    public static void json(HttpExchange exchange, int statusCode, Object payload) throws IOException {
        byte[] bytes = JsonUtil.toJson(payload).getBytes(StandardCharsets.UTF_8);
        bytes(exchange, statusCode, "application/json; charset=utf-8", bytes);
    }

    public static void error(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("error", message);
        json(exchange, statusCode, payload);
    }

    public static void noContent(HttpExchange exchange, int statusCode) throws IOException {
        applyDefaultHeaders(exchange.getResponseHeaders());
        exchange.sendResponseHeaders(statusCode, -1);
        exchange.close();
    }

    public static void bytes(HttpExchange exchange, int statusCode, String contentType, byte[] bytes)
        throws IOException {
        Headers headers = exchange.getResponseHeaders();
        applyDefaultHeaders(headers);
        headers.set("Content-Type", contentType);

        if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(statusCode, -1);
            exchange.close();
            return;
        }

        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static void applyDefaultHeaders(Headers headers) {
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
        headers.set("Cache-Control", "no-store");
    }
}
