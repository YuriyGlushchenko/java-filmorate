package ru.yandex.practicum.filmorate.exceptions.exceptions;

public class DuplicatedDataException extends RuntimeException {
    public DuplicatedDataException(String message) {
        super(message);
    }
}
