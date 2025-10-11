package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Film validFilm;

    @Autowired
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        validFilm = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

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

    @Test
    void createFilm_WithValidData_ShouldReturnCreatedFilm() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Test Film")))
                .andExpect(jsonPath("$.description", is("Test Description")))
                .andExpect(jsonPath("$.duration", is(120)));
    }

    @Test
    void createFilm_WithNullName_ShouldReturnValidationError() throws Exception {
        Film film = Film.builder()
                .name(null)
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_FAILED")));
    }

    @Test
    void createFilm_WithEmptyName_ShouldReturnValidationError() throws Exception {
        Film film = Film.builder()
                .name("")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_FAILED")));
    }

    @Test
    void createFilm_WithBlankName_ShouldReturnValidationError() throws Exception {
        Film film = Film.builder()
                .name("   ")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_FAILED")));
    }

    @Test
    void createFilm_WithTooLongDescription_ShouldReturnValidationError() throws Exception {
        String longDescription = "A".repeat(201);
        Film film = Film.builder()
                .name("Film Name")
                .description(longDescription)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_FAILED")));
    }

    @Test
    void createFilm_WithEarlyReleaseDate_ShouldReturnValidationError() throws Exception {
        Film film = Film.builder()
                .name("Film Name")
                .description("Description")
                .releaseDate(LocalDate.of(1890, 1, 1))
                .duration(120)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_FAILED")));
    }

    @Test
    void createFilm_WithNegativeDuration_ShouldReturnValidationError() throws Exception {
        Film film = Film.builder()
                .name("Film Name")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(-10)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_FAILED")));
    }

    @Test
    void createFilm_WithZeroDuration_ShouldReturnValidationError() throws Exception {
        Film film = Film.builder()
                .name("Film Name")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(0)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_FAILED")));
    }

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
        int filmId = createdFilm.getId();

        // Обновляем фильм
        Film updatedFilm = Film.builder()
                .id(filmId)
                .name("Updated Film")
                .description("Updated Description")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(150)
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(filmId)))
                .andExpect(jsonPath("$.name", is("Updated Film")))
                .andExpect(jsonPath("$.description", is("Updated Description")))
                .andExpect(jsonPath("$.duration", is(150)));
    }

    @Test
    void updateFilm_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        Film film = Film.builder()
                .id(999)
                .name("Non Existent Film")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("NOT_FOUND")));
    }

    @Test
    void updateFilm_WithNegativeId_ShouldReturnValidationError() throws Exception {
        Film film = Film.builder()
                .id(-1)
                .name("Film with negative ID")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("BAD_REQUEST")));
    }

    @Test
    void updateFilm_WithZeroId_ShouldReturnBAD_REQUEST() throws Exception {
        Film film = Film.builder()
                .id(0)
                .name("Film with zero ID")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("BAD_REQUEST")));
    }

    @Test
    void updateFilm_WithPartialData_ShouldReturnValidationError() throws Exception {
        // Создаем фильм
        String createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(createResponse, Film.class);
        int filmId = createdFilm.getId();

        // Обновляем только название (частичное обновление)
        Film partialUpdate = Film.builder()
                .id(filmId)
                .name("Only Name Updated")
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_FAILED")));

    }

    @Test
    void updateFilm_WithInvalidDescriptionLength_ShouldReturnValidationError() throws Exception {
        // Создаем фильм
        String createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(createResponse, Film.class);
        int filmId = createdFilm.getId();

        // Пытаемся обновить с слишком длинным описанием (должно быть проигнорировано)
        Film updateWithLongDescription = Film.builder()
                .id(filmId)
                .name("Updated Name")
                .description("A".repeat(250)) // Слишком длинное описание - должно быть проигнорировано
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(150)
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWithLongDescription)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_FAILED")));
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
        int filmId = createdFilm.getId();

        // Пытаемся обновить с ранней датой релиза (должна быть проигнорирована)
        Film updateWithEarlyDate = Film.builder()
                .id(filmId)
                .name("Updated Name")
                .description("Updated Description")
                .releaseDate(LocalDate.of(1890, 1, 1)) // Слишком ранняя дата - должна быть проигнорирована
                .duration(150)
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWithEarlyDate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_FAILED")));
    }

    @Test
    void updateFilm_WithNullName_ShouldReturnValidationError() throws Exception {
        // Создаем фильм
        String createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(createResponse, Film.class);
        int filmId = createdFilm.getId();

        // Обновляем с null именем (должно быть проигнорировано)
        Film updateWithNullName = Film.builder()
                .id(filmId)
                .name(null) // null имя - должно быть проигнорировано
                .description("Updated Description")
                .duration(150)
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWithNullName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_FAILED")));
    }

    @Test
    void updateFilm_WithEmptyName_ShouldReturnValidationError() throws Exception {
        // Создаем фильм
        String createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(createResponse, Film.class);
        int filmId = createdFilm.getId();

        // Обновляем с пустым именем (должно быть проигнорировано)
        Film updateWithEmptyName = Film.builder()
                .id(filmId)
                .name("") // Пустое имя - должно быть проигнорировано
                .description("Updated Description")
                .duration(150)
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWithEmptyName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_FAILED")));
    }

    @Test
    void updateFilm_WithNegativeDuration_ShouldReturnValidationError() throws Exception {
        // Создаем фильм
        String createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(createResponse, Film.class);
        int filmId = createdFilm.getId();

        // Пытаемся обновить с отрицательной продолжительностью (должна быть проигнорирована)
        Film updateWithNegativeDuration = Film.builder()
                .id(filmId)
                .name("Updated Name")
                .description("Updated Description")
                .duration(-10) // Отрицательная продолжительность - должна быть проигнорирована
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWithNegativeDuration)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_FAILED")));
    }

    @Test
    void createFilm_WithNotNullId_ShouldReturnBAD_REQUEST() throws Exception {
        Film invalidIdFilm = validFilm.toBuilder().id(1).build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidIdFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("BAD_REQUEST")));
    }

    @Test
    void createFilm_WithNotZeroId_ShouldReturnBAD_REQUEST() throws Exception {
        Film invalidIdFilm = validFilm.toBuilder().id(0).build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidIdFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("BAD_REQUEST")));
    }

}