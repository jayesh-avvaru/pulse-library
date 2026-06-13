package com.smartplaylist.service;

import com.smartplaylist.exception.ValidationException;
import com.smartplaylist.model.UserProfile;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthService {
    private final Map<String, UserProfile> users = new ConcurrentHashMap<>();

    public AuthService() {
        register(
            new UserProfile(
                "admin",
                "1234",
                "Admin Listener",
                List.of("Pop", "Electronic"),
                List.of("Focus", "Late Night")
            )
        );
        register(
            new UserProfile(
                "jayesh",
                "music123",
                "Jayesh",
                List.of("Indie", "R&B"),
                List.of("Chill", "Uplift")
            )
        );
        register(
            new UserProfile(
                "listener",
                "apple123",
                "Guest Listener",
                List.of("Ambient", "Pop"),
                List.of("Focus", "Chill")
            )
        );
    }

    public UserProfile authenticate(String username, String password) {
        UserProfile user = users.get(normalize(username));
        if (user == null || !user.passwordMatches(password)) {
            throw new ValidationException("Invalid username or password. Try admin / 1234.");
        }
        return user;
    }

    public UserProfile requireUser(String username) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("username is required.");
        }
        UserProfile user = users.get(normalize(username));
        if (user == null) {
            throw new ValidationException("Unknown user session. Please login again.");
        }
        return user;
    }

    private void register(UserProfile user) {
        users.put(normalize(user.getUsername()), user);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
