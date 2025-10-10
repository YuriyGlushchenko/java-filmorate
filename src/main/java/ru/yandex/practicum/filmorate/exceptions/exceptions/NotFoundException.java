package ru.yandex.practicum.filmorate.exceptions.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
