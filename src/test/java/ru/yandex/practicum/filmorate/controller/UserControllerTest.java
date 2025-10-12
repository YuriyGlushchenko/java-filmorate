package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest  // загружает полный контекст приложения Spring, имитируя запуск реального приложения.
@AutoConfigureMockMvc  // настраивает и создает экземпляр MockMvc для тестирования.
class UserControllerTest {

    @Autowired  // внедряется созданный и настроенный экземпляр MockMvc
    private MockMvc mockMvc;  // Тестирование Spring MVC контроллеров без поднятия HTTP сервера.


    private User validUser;

    @Autowired
    private ObjectMapper objectMapper;  // отвечает за сериализацию и десериализацию в spring

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .email("test@mail.ru")
                .login("testlogin")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void shouldRejectEmptyRequestBody() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidJson() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithNullEmail_ShouldReturn400() throws Exception {
        // CHECKSTYLE:OFF
        String userJson = """
                {
                    "login": "validlogin",
                    "birthday": "1990-01-01"
                }
                """;
        // CHECKSTYLE:ON

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WithInvalidEmail_ShouldReturn400() throws Exception {
        validUser.setEmail("invalid-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrorList[0].fieldName").value("email"))
                .andExpect(jsonPath("$.validationErrorList[0].message").value("Некорректный формат email"));
    }

    @Test
    void updateUser_WithInvalidId_ShouldReturn400() throws Exception {
        User user = User.builder()
                .id(-1)
                .email("test@mail.ru")
                .login("validlogin")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CONSTRAINT_VIOLATIONS"));
    }

    @Test
    void createUser_WithNotNullId_ShouldReturn400() throws Exception {
        validUser.setId(1);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CONSTRAINT_VIOLATIONS"));
    }

    @Test
    void updateUser_WithNonExistentId_ShouldReturn404() throws Exception {
        User user = User.builder()
                .id(999)
                .email("test@mail.ru")
                .login("validlogin")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    @Test
    void createUser_WithMultipleValidationErrors_ShouldReturnAllErrors() throws Exception {
        User user = User.builder()
                .email("invalid-email")
                .login("") // генерит 2 ошибки валидации
                .birthday(LocalDate.now().plusDays(1))
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrorList").isArray())
                .andExpect(jsonPath("$.validationErrorList.length()").value(4));
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsNull() throws Exception {
        // CHECKSTYLE:OFF
        String userJson = """
                {
                
                    "email": "test@example.com",
                    "login": "testlogin",
                    "birthday": "1990-01-01"
                }
                """;
        // CHECKSTYLE:ON

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testlogin"));
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsBlank() throws Exception {
        // CHECKSTYLE:OFF
        String userJson = """
                {
                    "email": "test@example.com",
                    "login": "testlogin",
                    "name": "   ",
                    "birthday": "1990-01-01"
                }
                """;
        // CHECKSTYLE:ON

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testlogin"));
    }

    @Test
    void shouldUpdateValidUserSuccessfully() throws Exception {
        // Исходный Json
        // CHECKSTYLE:OFF
        String createUserJson = """
                {
                    "email": "original@example.com",
                    "login": "originallogin",
                    "name": "Original Name",
                    "birthday": "1990-01-01"
                }
                """;
        // CHECKSTYLE:ON

        // отправляем запрос на создание, получаем ответ
        String createdUserResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Достаём ID созданного пользователя
        User createdUser = objectMapper.readValue(createdUserResponse, User.class);
        Integer userId = createdUser.getId();

        // Json для обновления
        // CHECKSTYLE:OFF
        String updateUserJson = String.format("""
                {
                    "id": %d,
                    "email": "updated@example.com",
                    "login": "updatedlogin",
                    "name": "Updated Name",
                    "birthday": "1995-05-15"
                }
                """, userId);
        // CHECKSTYLE:ON

        // Запрос на обновление
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateUserJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.login").value("updatedlogin"))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.birthday").value("1995-05-15"));
    }

    @Test
    void shouldUpdateUserWithNullNameAndSetLoginAsName() throws Exception {
        // CHECKSTYLE:OFF
        String createUserJson = """
                {
                    "email": "test@example.com",
                    "login": "testlogin",
                    "name": "Test Name",
                    "birthday": "1990-01-01"
                }
                """;
        // CHECKSTYLE:ON

        // Создаем пользователя
        String createdUserResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Получаем ID созданного пользователя
        User createdUser = objectMapper.readValue(createdUserResponse, User.class);
        Integer userId = createdUser.getId();

        // Обновляем пользователя с null именем
        // CHECKSTYLE:OFF
        String updateUserJson = String.format("""
                {
                    "id": %d,
                    "email": "updated@example.com",
                    "login": "newlogin",
                    "name": null,
                    "birthday": "1995-05-15"
                }
                """, userId);
        // CHECKSTYLE:ON

        // Проверяем, что при обновлении с null именем устанавливается логин
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateUserJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.login").value("newlogin"))
                .andExpect(jsonPath("$.name").value("newlogin")) // имя должно быть равно логину
                .andExpect(jsonPath("$.birthday").value("1995-05-15"));
    }

    @Test
    void shouldRejectUpdateWithNullId() throws Exception {
        User invalidUser = validUser.toBuilder().id(null).build();

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUpdateWithInvalidEmail() throws Exception {
        User invalidUser = validUser.toBuilder().email("bad-email").build();

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUpdateWithInvalidLogin() throws Exception {
        User invalidUser = validUser.toBuilder().login("   ").build();

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

}