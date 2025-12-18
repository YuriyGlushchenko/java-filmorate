package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SearchBy {
    TITLE("title"),
    DIRECTOR("director"),
    DIRECTOR_AND_TITLE("director,title");

    private final String value;

    SearchBy(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static SearchBy from(String searchBy) {
        if (searchBy == null || searchBy.isBlank()) {
            return DIRECTOR_AND_TITLE;
        }

        String normalized = searchBy.toLowerCase().replace(" ", "");

        // Крутая штука - паттерн матчинг в switch
        return switch (normalized) {
            case String s when s.contains("title") && s.contains("director") -> DIRECTOR_AND_TITLE;
            case String s when s.contains("title") -> TITLE;
            case String s when s.contains("director") -> DIRECTOR;
            default -> DIRECTOR_AND_TITLE;
        };
    }
}