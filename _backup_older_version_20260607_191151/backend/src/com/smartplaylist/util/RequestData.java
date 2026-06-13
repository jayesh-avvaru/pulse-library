package com.smartplaylist.util;

import com.smartplaylist.exception.ValidationException;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RequestData {
    private RequestData() {
    }

    public static Map<String, String> readForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return parseEncoded(body);
    }

    public static Map<String, String> query(HttpExchange exchange) {
        return parseEncoded(exchange.getRequestURI().getRawQuery());
    }

    public static String required(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            throw new ValidationException(key + " is required.");
        }
        return value.trim();
    }

    public static String optional(Map<String, String> values, String key, String fallback) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    public static int intValue(Map<String, String> values, String key, int fallback) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException error) {
            throw new ValidationException(key + " must be a number.");
        }
    }

    private static Map<String, String> parseEncoded(String encoded) {
        Map<String, String> values = new LinkedHashMap<>();
        if (encoded == null || encoded.isBlank()) {
            return values;
        }

        for (String pair : encoded.split("&")) {
            if (pair.isBlank()) {
                continue;
            }

            int separator = pair.indexOf('=');
            String key = separator >= 0 ? pair.substring(0, separator) : pair;
            String value = separator >= 0 ? pair.substring(separator + 1) : "";
            values.put(
                URLDecoder.decode(key, StandardCharsets.UTF_8),
                URLDecoder.decode(value, StandardCharsets.UTF_8)
            );
        }

        return values;
    }
}
