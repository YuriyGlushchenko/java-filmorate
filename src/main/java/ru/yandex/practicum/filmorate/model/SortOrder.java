package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SortOrder {
    YEAR("year"),
    LIKES("likes");

    private final String value;

    SortOrder(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static SortOrder from(String order) {
        if (order == null) {
            return YEAR;
        }

        return switch (order.toLowerCase()) {
            case "year" -> YEAR;
            case "likes" -> LIKES;
            default -> YEAR;
        };
    }

    // Для автоматической десериализации Spring
    @JsonCreator
    public static SortOrder forValue(String value) {
        return from(value);
    }
}