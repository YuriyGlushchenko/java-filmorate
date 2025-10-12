package ru.yandex.practicum.filmorate.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AfterValidator.class) // Указывает класс-валидатор
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface After {
    String message() default "Дата должна быть после {value}";

    Class<?>[] groups() default {};  // для групп валидации, например валидация только в create, а в update нет

    Class<? extends Payload>[] payload() default {}; // может передавать severity уровень важности ошибки, без этого не работает

    String value(); // Параметр для указания опорной даты
}
