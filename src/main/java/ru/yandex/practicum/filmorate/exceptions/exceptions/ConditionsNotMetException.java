package ru.yandex.practicum.filmorate.exceptions.exceptions;

public class ConditionsNotMetException extends RuntimeException {
    public ConditionsNotMetException(String message) {
        super(message);
    }
}
