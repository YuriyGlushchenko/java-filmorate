package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exceptions.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserController userController;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .email("test@mail.ru")
                .login("testLogin")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void createUser_WithValidData_ShouldReturnUser() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("test@mail.ru"))
                .andExpect(jsonPath("$.login").value("testLogin"))
                .andExpect(jsonPath("$.name").value("Test Name"));
    }


    @Test
    void createUser_WithEmptyName_ShouldUseLoginAsName() throws Exception {
        validUser.setName("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testLogin"));
    }

    @Test
    void createUser_WithNullName_ShouldUseLoginAsName() throws Exception {
        validUser.setName(null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testLogin"));
    }

//    @Test
//    void createUser_WithInvalidEmail_ShouldThrowValidationException() {
//
//        validUser.setEmail("invalid-email");
//
//
//        // Создаем BindingResult с ошибками вручную
//        BindingResult bindingResult = new BeanPropertyBindingResult(validUser, "user");
//
//        // Добавляем ошибку валидации для поля email
//        bindingResult.rejectValue("email", "invalid.email", "Некорректный email");
//
//        ValidationException exception = assertThrows(ValidationException.class,
//                () -> userController.create(validUser, bindingResult));
//
//        assertEquals("Параметры пользователя недопустимы", exception.getMessage());
//    }

    @Test
    void createUser_WithNullEmail_ShouldThrowValidationException() {
        validUser.setEmail(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(validUser));
        assertEquals("Параметры пользователя недопустимы", exception.getMessage());
    }

    @Test
    void createUser_WithEmptyEmail_ShouldThrowValidationException() {
        validUser.setEmail("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(validUser));
        assertEquals("Параметры пользователя недопустимы", exception.getMessage());
    }

    @Test
    void createUser_WithEmailWithoutAtSymbol_ShouldThrowValidationException() {
        validUser.setEmail("emailwithoutat.com");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(validUser));
        assertEquals("Параметры пользователя недопустимы", exception.getMessage());
    }

    @Test
    void createUser_WithInvalidLogin_ShouldThrowValidationException() {
        validUser.setLogin("invalid login with spaces");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(validUser));
        assertEquals("Параметры пользователя недопустимы", exception.getMessage());
    }

    @Test
    void createUser_WithNullLogin_ShouldThrowValidationException() {
        validUser.setLogin(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(validUser));
        assertEquals("Параметры пользователя недопустимы", exception.getMessage());
    }

    @Test
    void createUser_WithEmptyLogin_ShouldThrowValidationException() {
        validUser.setLogin("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(validUser));
        assertEquals("Параметры пользователя недопустимы", exception.getMessage());
    }

    @Test
    void createUser_WithFutureBirthday_ShouldThrowValidationException() {
        validUser.setBirthday(LocalDate.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(validUser));
        assertEquals("Параметры пользователя недопустимы", exception.getMessage());
    }

    @Test
    void createUser_WithTodayBirthday_ShouldBeValid() throws Exception {
        validUser.setBirthday(LocalDate.now());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.birthday").exists());
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() throws Exception {
        // Сначала создаем пользователя
        User createdUser = userController.create(validUser);

        // Обновляем данные
        User updateUser = User.builder()
                .id(createdUser.getId())
                .email("updated@mail.ru")
                .login("updatedLogin")
                .name("Updated Name")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@mail.ru"))
                .andExpect(jsonPath("$.login").value("updatedLogin"))
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateUser_WithInvalidId_ShouldThrowValidationException() {
        validUser.setId(-1);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.update(validUser));
        assertEquals("Id должен быть корректно указан (положительное целое число)", exception.getMessage());
    }

    @Test
    void updateUser_WithNonExistentId_ShouldThrowNotFoundException() {
        validUser.setId(9999);

        assertThrows(NotFoundException.class,
                () -> userController.update(validUser));
    }

    @Test
    void updateUser_PartialUpdate_ShouldUpdateOnlyProvidedFields() throws Exception {
        // Сначала создаем пользователя
        User createdUser = userController.create(validUser);

        // Обновляем только email
        User updateUser = User.builder()
                .id(createdUser.getId())
                .email("partial@mail.ru")
                .login(createdUser.getLogin()) // остальные поля остаются прежними
                .name(createdUser.getName())
                .birthday(createdUser.getBirthday())
                .build();

        User updated = userController.update(updateUser);

        assertEquals("partial@mail.ru", updated.getEmail());
        assertEquals(createdUser.getLogin(), updated.getLogin());
        assertEquals(createdUser.getName(), updated.getName());
        assertEquals(createdUser.getBirthday(), updated.getBirthday());
    }

    @Test
    void findAll_ShouldReturnAllUsers() throws Exception {
        // Очищаем существующих пользователей перед тестом
        userController.findAll().clear();

        userController.create(validUser);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void createUser_WithEmptyRequestBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

//     Тесты для граничных условий логина
    @Test
    void createUser_WithLoginContainingSpaces_ShouldThrowValidationException() {
        validUser.setLogin("login with spaces");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(validUser));
        assertEquals("Параметры пользователя недопустимы", exception.getMessage());
    }

    @Test
    void createUser_WithLoginContainingTabs_ShouldThrowValidationException() {
        validUser.setLogin("login\twith\ttabs");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(validUser));
        assertEquals("Параметры пользователя недопустимы", exception.getMessage());
    }

    @Test
    void createUser_WithValidLoginWithoutSpaces_ShouldBeValid() throws Exception {
        validUser.setLogin("validLogin123");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("validLogin123"));
    }
}