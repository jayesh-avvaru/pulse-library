package com.pulselibrary.model;

public enum MoodType {
    UPLIFT("Uplift"),
    ENERGY("Energy"),
    CHILL("Chill"),
    LATE_NIGHT("Late Night"),
    FOCUS("Focus"),
    WORKOUT("Workout");

    private final String label;

    MoodType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static MoodType fromString(String value) {
        if (value == null || value.isBlank() || "ANY".equalsIgnoreCase(value.trim())) {
            return null;
        }
        String normalized = value.trim().toUpperCase().replace(' ', '_');
        for (MoodType mood : values()) {
            if (mood.name().equals(normalized) || mood.label.equalsIgnoreCase(value.trim())) {
                return mood;
            }
        }
        throw new IllegalArgumentException("Unknown mood: " + value);
    }
}

