package ru.yandex.practicum.filmorate.exceptions.responses;

import lombok.Data;

@Data
public class ErrorMessage {
    private final String errorCode;
    private final String message;
    private final String error;      // костыль для тестов add-director, они требуют наличия именно поля "error"

    public ErrorMessage(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.error = message;
    }
}
