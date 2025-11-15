package ru.yandex.practicum.filmorate.model;

public enum FriendshipStatus {
    PENDING(1),
    CONFIRMED(2);

    private final int statusId;

    FriendshipStatus(int code) {
        this.statusId = code;
    }

    public int getStatusId() {
        return statusId;
    }

    public static FriendshipStatus fromId(int id) {
        return switch (id) {
            case 1 -> PENDING;
            case 2 -> CONFIRMED;
            default -> throw new IllegalArgumentException("Неизвестный код статуса: " + id);
        };
    }
}
