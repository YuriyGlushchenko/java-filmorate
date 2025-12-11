package ru.yandex.practicum.filmorate.exceptions.exceptions;

public class ParameterNotValidException extends RuntimeException {
    public ParameterNotValidException(String message) {
        super(message);
    }
}
