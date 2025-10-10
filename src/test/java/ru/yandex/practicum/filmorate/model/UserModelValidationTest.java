package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserModelValidationTest {
    private static Validator validator;
    private User validUser;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();  // Валидатор из spring`а
        }
    }

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .email("valid@example.com")
                .login("validlogin")
                .name("Valid Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    // ========== POSITIVE TESTS - Валидные данные ==========

    @Test
    void shouldCreateValidUser() {
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty(), "Валидный пользователь не должен иметь нарушений валидации");
    }

    @Test
    void shouldCreateUserWithMinimumValidData() {
        User user = User.builder()
                .email("a@b.c")
                .login("a")
                .birthday(LocalDate.now())
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Минимально валидные данные должны быть приняты");
    }

    @Test
    void shouldCreateUserWithComplexEmail() {
        validUser.setEmail("test.user+tag@sub.domain.long.com");

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty(), "Сложный email должен быть допустимым");
    }

    // ========== NEGATIVE TESTS - Email валидация ==========

    @Test
    void shouldFailWhenEmailIsBlank() {
        validUser.setEmail("");

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Email не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        validUser.setEmail("invalid-email");

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Некорректный формат email", violations.iterator().next().getMessage());
    }

    // ========== NEGATIVE TESTS - Login валидация ==========

    @Test
    void shouldFailWhenLoginIsBlank() {
        validUser.setLogin("");

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());

        Optional<String> notBlankViolationMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .filter(message -> message.equals("Логин не может быть пустым"))
                .findFirst();

        assertTrue(notBlankViolationMessage.isPresent());
    }

    @Test
    void shouldFailWhenLoginContainsSpaces() {
        validUser.setLogin("test login");

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Логин не должен содержать пробелы", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenLoginContainsMultipleSpaces() {
        validUser.setLogin("test   multipleSpaceslogin");

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Логин не должен содержать пробелы")));
    }

    // ========== NEGATIVE TESTS - Birthday валидация ==========

    @Test
    void shouldFailWhenBirthdayIsInFuture() {
        validUser.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Дата рождения не может быть в будущем", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenBirthdayIsFarInFuture() {
        validUser.setBirthday(LocalDate.now().plusYears(10));

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Дата рождения не может быть в будущем", violations.iterator().next().getMessage());
    }

    // ========== MULTIPLE VALIDATION ERRORS ==========

    @Test
    void shouldHandleMultipleValidationErrors() {
        User user = User.builder()
                .email("invalid-email")
                .login("")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertEquals(4, violations.size());  // еще побочно срабатывает "Логин не должен содержать пробелы"
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Некорректный формат email")));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Логин не может быть пустым")));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Дата рождения не может быть в будущем")));
    }
}