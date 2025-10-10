package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("GET /users: Запрос на получение всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("POST /films: Создание фильма с названием {}", film.getName());
        log.trace("Полные данные пользователя: {}", film);

        film.setId(getNextId());
        films.put(film.getId(), film);

        log.debug("Фильм с названием {} успешно создан с ID: {}", film.getName(), film.getId());
        return film;
    }

    // В ТЗ не описано, как быть, если одно из полей невалидно, обновлять ли остальные поля, которые корректные,
    // или полностью отклонять такой запрос на обновление. Если полностью отклонять, то можно, конечно, сделать проще,
    // через @Valid как в методе create.
    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.debug("Обновление фильма с ID: {}", newFilm.getId());
        log.trace("Полные данные фильма для обновления: {}", newFilm);

        if (newFilm.getId() <= 0) {
            log.warn("Передан фильм с некорректным id {}", newFilm.getId());
            throw new ValidationException("Id", newFilm.getId(), "Id должен быть корректно указан (положительное целое число)");
        }

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            if (newFilm.getName() != null && !newFilm.getName().isBlank()) {
                oldFilm.setName(newFilm.getName());
                log.debug("Обновлено название фильма: {}", newFilm.getName());
            }
            if (newFilm.getDescription() != null && newFilm.getDescription().length() < 200) {
                oldFilm.setDescription(newFilm.getDescription());
                log.debug("Обновлено описание фильма: {}", newFilm.getDescription());
            }
            if (newFilm.getDuration() > 0) {
                oldFilm.setDuration(newFilm.getDuration());
                log.debug("Обновлена продолжительность фильма: {}", newFilm.getDuration());
            }
            if (newFilm.getReleaseDate() != null && newFilm.getReleaseDate().isAfter(LocalDate.of(1895, 12, 28))) {
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
                log.debug("Обновлена дата выхода: {}", newFilm.getReleaseDate());
            }

            log.info("Фильма с ID {} успешно обновлен", newFilm.getId());
            return oldFilm;
        }
        throw new NotFoundException("Фильма с id = " + newFilm.getId() + " не найдено");
    }

    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
