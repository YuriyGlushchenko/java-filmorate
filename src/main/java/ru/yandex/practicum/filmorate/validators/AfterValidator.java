package ru.yandex.practicum.filmorate.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class AfterValidator implements ConstraintValidator<After, LocalDate> {

    private LocalDate targetDate;

    @Override
    public void initialize(After constraintAnnotation) {
        // Получаем дату из параметра аннотации
        targetDate = LocalDate.parse(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {

        if (value == null) { // можно сделать наоборот, тогда дата будет обязательной
            return true;
        }

        return value.isAfter(targetDate);  // сама валидация
    }
}