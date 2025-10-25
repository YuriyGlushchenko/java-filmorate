package ru.yandex.practicum.filmorate.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.exceptions.responses.ErrorMessage;
import ru.yandex.practicum.filmorate.exceptions.responses.ValidationError;
import ru.yandex.practicum.filmorate.exceptions.responses.ValidationErrorResponse;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
// Автоматически добавляет @ResponseBody ко всем методам. Возвращаемые объекты автоматически сериализуются в JSON.
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Перехват исключения при валидации аргументов тела запроса с @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        // MethodArgumentNotValidException — исключение, которое выбрасывается Spring при неудачной валидации с @Valid

        // Получаем все ошибки валидации полей
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        List<ValidationError> validationErrors = fieldErrors.stream()
                .map(fieldError -> new ValidationError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage(),
                        fieldError.getRejectedValue()
                ))
                .collect(Collectors.toList());

        return new ValidationErrorResponse("VALIDATION_FAILED", validationErrors);
    }


    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleValidationException(ValidationException ex) {

        List<ValidationError> validationErrors = List.of(
                new ValidationError(ex.getFieldName(), ex.getMessage(), ex.getRejectedValue()));

        return new ValidationErrorResponse("VALIDATION_FAILED", validationErrors);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleNotFoundException(NotFoundException ex) {

        return new ErrorMessage("NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleConstraintViolationException(ConstraintViolationException ex) {

        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();

        List<ValidationError> validationErrors = violations.stream()
                .map(violation -> new ValidationError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage(),
                        violation.getInvalidValue()
                ))
                .collect(Collectors.toList());

        return new ValidationErrorResponse("CONSTRAINT_VIOLATIONS", validationErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {

        return new ErrorMessage("BAD_REQUEST", "Required request body is missing");
    }


}

