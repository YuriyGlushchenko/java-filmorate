package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Film validFilm;

    @BeforeEach
    void setUp() {
        MpaRating mpa = MpaRating.builder()
                .id(1)
                .name("G")
                .build();

        validFilm = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build();
    }

    // Тесты для создания фильма
    @Test
    void createFilm_WithValidData_ShouldReturnCreatedFilm() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Test Film"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.duration").value(120))
                .andExpect(jsonPath("$.mpa.id").value(1));
    }

    @Test
    void createFilm_WithEmptyBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_WithInvalidJSON_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ bad json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_WithNullBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Тесты валидации при создании
    @Test
    void createFilm_WithNullName_ShouldReturnValidationError() throws Exception {
        Film film = validFilm.toBuilder()
                .name(null)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_WithEmptyName_ShouldReturnValidationError() throws Exception {
        Film film = validFilm.toBuilder()
                .name("")
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_WithBlankName_ShouldReturnValidationError() throws Exception {
        Film film = validFilm.toBuilder()
                .name("   ")
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_WithTooLongDescription_ShouldReturnValidationError() throws Exception {
        Film film = validFilm.toBuilder()
                .description("A".repeat(201))
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_WithEarlyReleaseDate_ShouldReturnValidationError() throws Exception {
        Film film = validFilm.toBuilder()
                .releaseDate(LocalDate.of(1890, 1, 1))
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_WithNegativeDuration_ShouldReturnValidationError() throws Exception {
        Film film = validFilm.toBuilder()
                .duration(-10)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_WithZeroDuration_ShouldReturnValidationError() throws Exception {
        Film film = validFilm.toBuilder()
                .duration(0)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFilm_WithNotNullId_ShouldReturnBadRequest() throws Exception {
        Film film = validFilm.toBuilder()
                .id(1)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    // Тесты для обновления фильма
    @Test
    void updateFilm_WithValidData_ShouldReturnUpdatedFilm() throws Exception {
        // Сначала создаем фильм
        String createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(createResponse, Film.class);

        // Обновляем фильм
        Film updatedFilm = createdFilm.toBuilder()
                .name("Updated Film")
                .description("Updated Description")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(150)
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdFilm.getId()))
                .andExpect(jsonPath("$.name").value("Updated Film"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.duration").value(150));
    }

    @Test
    void updateFilm_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        Film film = validFilm.toBuilder()
                .id(999)
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateFilm_WithEmptyBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFilm_WithInvalidJSON_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ bad json }"))
                .andExpect(status().isBadRequest());
    }

    // Тесты валидации при обновлении
    @Test
    void updateFilm_WithNullName_ShouldReturnValidationError() throws Exception {
        String createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(createResponse, Film.class);

        Film updateFilm = createdFilm.toBuilder()
                .name(null)
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFilm_WithEmptyName_ShouldReturnValidationError() throws Exception {
        String createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(createResponse, Film.class);

        Film updateFilm = createdFilm.toBuilder()
                .name("")
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFilm_WithTooLongDescription_ShouldReturnValidationError() throws Exception {
        String createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(createResponse, Film.class);

        Film updateFilm = createdFilm.toBuilder()
                .description("A".repeat(201))
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFilm_WithEarlyReleaseDate_ShouldReturnValidationError() throws Exception {
        String createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(createResponse, Film.class);

        Film updateFilm = createdFilm.toBuilder()
                .releaseDate(LocalDate.of(1890, 1, 1))
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFilm_WithNegativeDuration_ShouldReturnValidationError() throws Exception {
        String createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(createResponse, Film.class);

        Film updateFilm = createdFilm.toBuilder()
                .duration(-10)
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getFilmById_WithValidId_ShouldReturnFilm() throws Exception {
        // Создаем фильм
        String createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(createResponse, Film.class);

        // Получаем фильм по ID
        mockMvc.perform(get("/films/{id}", createdFilm.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdFilm.getId()))
                .andExpect(jsonPath("$.name").value("Test Film"));
    }

    @Test
    void getFilmById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/films/{id}", 999))
                .andExpect(status().isNotFound());
    }

    // Тесты для работы с лайками
    @Test
    void addLike_WithValidData_ShouldAddLike() throws Exception {
        // Создаем фильм
        String createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(createResponse, Film.class);

        // Добавляем лайк
        mockMvc.perform(put("/films/{id}/like/{userId}", createdFilm.getId(), 1))
                .andExpect(status().isOk());
    }

    @Test
    void removeLike_WithValidData_ShouldRemoveLike() throws Exception {
        // Создаем фильм
        String createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(createResponse, Film.class);

        // Добавляем лайк
        mockMvc.perform(put("/films/{id}/like/{userId}", createdFilm.getId(), 1))
                .andExpect(status().isOk());

        // Удаляем лайк
        mockMvc.perform(delete("/films/{id}/like/{userId}", createdFilm.getId(), 1))
                .andExpect(status().isOk());
    }

    @Test
    void getPopularFilms_ShouldReturnPopularFilms() throws Exception {
        // Создаем фильм
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk());

        // Получаем популярные фильмы
        mockMvc.perform(get("/films/popular")
                        .param("count", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getPopularFilms_WithDefaultCount_ShouldReturnFilms() throws Exception {
        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}