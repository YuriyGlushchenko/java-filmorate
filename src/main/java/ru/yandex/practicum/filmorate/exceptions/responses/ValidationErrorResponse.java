package ru.yandex.practicum.filmorate.exceptions.responses;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ValidationErrorResponse {
    private String errorCode;
    private List<ValidationError> validationErrorList;
    private Date timestamp;

    public ValidationErrorResponse(String errorCode, List<ValidationError> validationErrorsList) {
        this.errorCode = errorCode;
        this.validationErrorList = validationErrorsList;
        this.timestamp = new Date();
    }

}