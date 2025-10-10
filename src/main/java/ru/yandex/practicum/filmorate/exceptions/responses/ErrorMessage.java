package ru.yandex.practicum.filmorate.exceptions.responses;

import lombok.Data;

@Data
public class ErrorMessage {
    private final String code;
    private final String message;

}