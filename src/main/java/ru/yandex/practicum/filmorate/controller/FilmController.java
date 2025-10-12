package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
@Validated
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("GET /users: Запрос на получение всех фильмов");
        return films.values();
    }

    @PostMapping
    @Validated({Marker.OnCreate.class})
    public Film create(@Valid @RequestBody Film film) {
        log.info("POST /films: Создание фильма с названием {}", film.getName());
        log.trace("Полные данные пользователя: {}", film);

        film.setId(getNextId());
        films.put(film.getId(), film);

        log.debug("Фильм с названием {} успешно создан с ID: {}", film.getName(), film.getId());
        return film;
    }

    @PutMapping
    @Validated({Marker.OnUpdate.class})
    public Film update(@RequestBody @Valid Film newFilm) {
        log.debug("Обновление фильма с ID: {}", newFilm.getId());
        log.trace("Полные данные фильма для обновления: {}", newFilm);

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setDuration(newFilm.getDuration());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());

            log.info("Фильм с ID {} успешно обновлен", newFilm.getId());
            return oldFilm;
        }
        throw new NotFoundException("Фильма с id = " + newFilm.getId() + " не найдено");
    }

    private Integer getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
