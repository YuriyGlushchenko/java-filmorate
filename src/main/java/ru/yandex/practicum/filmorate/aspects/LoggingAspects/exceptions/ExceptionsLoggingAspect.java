package ru.yandex.practicum.filmorate.aspects.LoggingAspects.exceptions;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.exceptions.ValidationException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
public class ExceptionsLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionsLoggingAspect.class);

    @Pointcut("execution(* ru.yandex.practicum.filmorate.exceptions.GlobalExceptionHandler.*(..))")
    public void exceptionHandlerMethods() {}

    // Единый метод для логирования всех обработчиков исключений
    @Before("exceptionHandlerMethods()")
    public void logExceptionHandler(JoinPoint joinPoint) {
        String handlerMethodName = joinPoint.getSignature().getName();

        // Получаем перехваченное исключение (первый аргумент метода)
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof Exception) {
            Exception exception = (Exception) args[0];

            // Формируем общее сообщение логирования
            String logMessage = buildLogMessage(handlerMethodName, exception);
            logger.warn(logMessage);
        } else {
            logger.warn("Вызван обработчик {} без исключения", handlerMethodName);
        }
    }

    private String buildLogMessage(String handlerMethodName, Exception exception) {
        StringBuilder message = new StringBuilder();
        message.append("GlobalExceptionHandler -> обработчик: ").append(handlerMethodName)
                .append(" | Тип исключения: ").append(exception.getClass().getSimpleName())
                .append(" | Сообщение: ").append(exception.getMessage());

        // Добавляем детальную информацию в зависимости от типа исключения если она есть
        switch (exception) {
            case MethodArgumentNotValidException methodArgumentNotValidException ->
                    message.append(" | ").append(getValidationDetails(methodArgumentNotValidException));
            case ValidationException validationException ->
                    message.append(" | ").append(getValidationExceptionDetails(validationException));
            case ConstraintViolationException constraintViolationException ->
                    message.append(" | ").append(getConstraintViolationDetails(constraintViolationException));
            case NotFoundException notFoundException ->
                    message.append(" | ").append(getNotFoundExceptionDetails(notFoundException));
            default -> {
            }
        }

        return message.toString();
    }

    private String getValidationDetails(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        return fieldErrors.stream()
                .map(error -> String.format(
                        "Поле '%s': значение '%s' - %s",
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.joining("; "));
    }

    private String getValidationExceptionDetails(ValidationException ex) {
        return String.format("Поле '%s': значение '%s' - %s",
                ex.getFieldName(),
                ex.getRejectedValue(),
                ex.getMessage());
    }

    private String getConstraintViolationDetails(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();

        return violations.stream()
                .map(violation -> String.format(
                        "Поле '%s': значение '%s' - %s",
                        violation.getPropertyPath().toString(),
                        violation.getInvalidValue(),
                        violation.getMessage()
                ))
                .collect(Collectors.joining("; "));
    }

    private String getNotFoundExceptionDetails(NotFoundException ex) {
        StackTraceElement topElement = ex.getStackTrace()[0];
        return String.format("Источник: %s.%s",
                topElement.getClassName(),
                topElement.getMethodName());
    }
}