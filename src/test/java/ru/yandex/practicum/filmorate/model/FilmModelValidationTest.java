package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmModelValidationTest {

    private static Validator validator;

    private Film validFilm;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @BeforeEach
    void setUp() {
        validFilm = Film.builder()
                .name("Valid Film")
                .description("This is a valid film description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
    }

    @Test
    void shouldCreateValidFilm() {
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);

        assertTrue(violations.isEmpty(), "Валидный фильм должен проходить валидацию");
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Film invalidFilm = validFilm.toBuilder().name("").build();

        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);
        assertFalse(violations.isEmpty(), "Должна быть ошибка валидации при пустом названии");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("Название фильма не может быть пустым", violation.getMessage());
        assertEquals("name", violation.getPropertyPath().toString());
    }

    @Test
    void shouldFailWhenNameIsNull() {
        Film invalidFilm = validFilm.toBuilder().name(null).build();

        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);

        assertFalse(violations.isEmpty(), "Должна быть ошибка валидации при названии null");

        boolean hasNameViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        assertTrue(hasNameViolation, "Должна быть ошибка для поля name");

    }

    @Test
    void shouldFailWhenDescriptionExceeds200Characters() {
        String longDescription = "A".repeat(201);
        Film invalidFilm = validFilm.toBuilder().description(longDescription).build();

        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);
        assertFalse(violations.isEmpty(), "Должны быть нарушения при описании длиннее 200 символов");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("Описание не должно превышать 200 символов", violation.getMessage());
        assertEquals("description", violation.getPropertyPath().toString());  // ошибка именно по полю description
    }

    @Test
    void shouldPassWhenDescriptionIsExactly200Characters() {
        String exactLengthDescription = "A".repeat(200); // Ровно 200 символов
        validFilm.setDescription(exactLengthDescription);

        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Описание длиной 200 символов должно быть валидным");
    }

    @Test
    void shouldFailWhenReleaseDateIsBefore1895_12_28() {
        Film invalidFilm = validFilm.toBuilder().releaseDate(LocalDate.of(1895, 12, 27)).build();

        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);

        assertFalse(violations.isEmpty(), "Должны быть нарушения при дате релиза до 28 декабря 1895");
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("Дата релиза должна быть после 28 декабря 1895 года", violation.getMessage());
        assertEquals("releaseDate", violation.getPropertyPath().toString());
    }

    @Test
    void shouldPassWhenReleaseDateIsExactly1895_12_28() {
        Film invalidFilm = validFilm.toBuilder().releaseDate(LocalDate.of(1895, 12, 28)).build();

        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);
        // Проверяем, что есть нарушения, так как дата должна быть ПОСЛЕ 28.12.1895
        assertFalse(violations.isEmpty(), "Дата 28.12.1895 должна вызывать нарушение (должна быть ПОСЛЕ)");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("releaseDate", violation.getPropertyPath().toString());
    }

    @Test
    void shouldPassWhenReleaseDateIsAfter1895_12_28() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 29));  // На день после допустимой даты

        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Дата после 28.12.1895 должна быть валидной");
    }

    @Test
    void shouldFailWhenReleaseDateIsNull() {
        Film invalidFilm = validFilm.toBuilder().releaseDate(null).build();

        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);
        assertFalse(violations.isEmpty(), "Должны быть нарушения при null дате релиза");

        boolean hasReleaseDateViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("releaseDate"));
        assertTrue(hasReleaseDateViolation, "Должно быть нарушение для поля releaseDate");
    }

    @Test
    void shouldFailWhenDurationIsZero() {
        Film invalidFilm = validFilm.toBuilder().duration(0).build();

        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);
        assertFalse(violations.isEmpty(), "Должны быть нарушения при нулевой продолжительности");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("Продолжительность должна быть положительной", violation.getMessage());
        assertEquals("duration", violation.getPropertyPath().toString());
    }

    @Test
    void shouldFailWhenDurationIsNegative() {
        Film invalidFilm = validFilm.toBuilder().duration(-10).build();

        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);
        assertFalse(violations.isEmpty(), "Должны быть нарушения при отрицательной продолжительности");

        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("Продолжительность должна быть положительной", violation.getMessage());
        assertEquals("duration", violation.getPropertyPath().toString());
    }

    @Test
    void shouldPassWhenDurationIsPositive() {
        validFilm.setDuration(1);

        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Положительная продолжительность должна быть валидной");
    }

    @Test
    void shouldHandleMultipleValidationErrors() {
        Film film = Film.builder()
                .name("") // Пустое название
                .description("A".repeat(201)) // Слишком длинное описание
                .releaseDate(LocalDate.of(1890, 1, 1)) // Слишком ранняя дата
                .duration(-10) // Отрицательная продолжительность
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(4, violations.size(), "Должно быть 4 нарушения валидации");

        // Проверяем, что все ожидаемые поля имеют нарушения
        Set<String> violatedFields = Set.of("name", "description", "releaseDate", "duration");
        Set<String> actualViolatedFields = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(violatedFields, actualViolatedFields, "Должны быть нарушения для всех полей");
    }

    @Test
    void shouldValidateFilmWithMinimalValidData() {
        Film film = Film.builder()
                .name("A") // Минимальное валидное название
                .description("") // Пустое описание
                .releaseDate(LocalDate.of(1895, 12, 29)) // Минимальная валидная дата
                .duration(1) // Минимальная валидная продолжительность
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Фильм с минимальными валидными данными должен проходить валидацию");
    }
}