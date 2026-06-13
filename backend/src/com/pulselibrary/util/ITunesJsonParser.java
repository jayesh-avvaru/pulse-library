package com.pulselibrary.util;

import com.pulselibrary.dto.SongDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ITunesJsonParser {
    private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{[^{}]*(?:\\{[^{}]*\\}[^{}]*)*\\}");

    private ITunesJsonParser() {
    }

    public static List<SongDTO> parseResults(String json) {
        List<SongDTO> songs = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return songs;
        }

        int resultsIndex = json.indexOf("\"results\"");
        if (resultsIndex < 0) {
            return songs;
        }

        int arrayStart = json.indexOf('[', resultsIndex);
        int arrayEnd = findMatchingBracket(json, arrayStart);
        if (arrayStart < 0 || arrayEnd < 0) {
            return songs;
        }

        String resultsArray = json.substring(arrayStart + 1, arrayEnd);
        Matcher matcher = OBJECT_PATTERN.matcher(resultsArray);
        while (matcher.find()) {
            SongDTO song = parseObject(matcher.group());
            if (song.getTitle() != null && song.getArtist() != null) {
                songs.add(song);
            }
        }
        return songs;
    }

    private static SongDTO parseObject(String objectJson) {
        SongDTO song = new SongDTO();
        song.setId(parseLong(extractStringValue(objectJson, "trackId")));
        song.setTitle(extractStringValue(objectJson, "trackName"));
        song.setArtist(extractStringValue(objectJson, "artistName"));
        song.setAlbum(extractStringValue(objectJson, "collectionName"));
        song.setArtworkUrl(extractStringValue(objectJson, "artworkUrl100"));
        song.setPreviewUrl(extractStringValue(objectJson, "previewUrl"));
        song.setDuration(parseLong(extractStringValue(objectJson, "trackTimeMillis")));
        song.setGenre(extractStringValue(objectJson, "primaryGenreName"));
        return song;
    }

    private static String extractStringValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(" +
            "\"((?:\\\\.|[^\"\\\\])*)\"|" +
            "(null|-?\\d+)" +
            ")");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        if (matcher.group(2) != null) {
            return unescape(matcher.group(2));
        }
        String raw = matcher.group(3);
        return "null".equals(raw) ? null : raw;
    }

    private static Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String unescape(String value) {
        return value
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\/", "/")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t");
    }

    private static int findMatchingBracket(String json, int start) {
        if (start < 0) {
            return -1;
        }
        int depth = 0;
        for (int i = start; i < json.length(); i++) {
            char current = json.charAt(i);
            if (current == '[') {
                depth++;
            } else if (current == ']') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
}
