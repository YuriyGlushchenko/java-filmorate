package ru.yandex.practicum.filmorate.model;

public enum SortOrder {
    YEAR, LIKES;

    // Преобразует строку в элемент перечисления
    public static SortOrder from(String order) {
        return switch (order.toLowerCase()) {
            case "year" -> YEAR;
            case "likes" -> LIKES;
            default -> null;
        };
    }
}