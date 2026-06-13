package com.pulselibrary.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class JsonParser {
    private JsonParser() {
    }

    public static Map<String, String> parseObject(String json) {
        Map<String, String> result = new LinkedHashMap<>();
        if (json == null || json.isBlank()) {
            return result;
        }

        String trimmed = json.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return result;
        }

        String body = trimmed.substring(1, trimmed.length() - 1).trim();
        if (body.isEmpty()) {
            return result;
        }

        int index = 0;
        while (index < body.length()) {
            int keyStart = body.indexOf('"', index);
            if (keyStart < 0) {
                break;
            }
            int keyEnd = body.indexOf('"', keyStart + 1);
            if (keyEnd < 0) {
                break;
            }
            String key = body.substring(keyStart + 1, keyEnd);
            int colon = body.indexOf(':', keyEnd);
            if (colon < 0) {
                break;
            }

            int valueStart = colon + 1;
            while (valueStart < body.length() && Character.isWhitespace(body.charAt(valueStart))) {
                valueStart++;
            }

            String value;
            if (valueStart < body.length() && body.charAt(valueStart) == '"') {
                int valueEnd = body.indexOf('"', valueStart + 1);
                value = body.substring(valueStart + 1, valueEnd);
                index = valueEnd + 1;
            } else {
                int comma = body.indexOf(',', valueStart);
                int end = comma < 0 ? body.length() : comma;
                value = body.substring(valueStart, end).trim();
                if (value.endsWith("}")) {
                    value = value.substring(0, value.length() - 1).trim();
                }
                index = end + 1;
            }

            result.put(key, value);
            int nextComma = body.indexOf(',', index);
            index = nextComma < 0 ? body.length() : nextComma + 1;
        }

        return result;
    }
}

