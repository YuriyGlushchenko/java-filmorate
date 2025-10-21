package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Component
public class InMemoryFilmStorage implements FilmStorage{
    private final Map<Integer, Film> films = new HashMap<>();

    public Collection<Film> findAll() {
//        log.info("GET /users: Запрос на получение всех фильмов");
        return films.values();
    }

    public Film create(Film film) {
//        log.info("POST /films: Создание фильма с названием {}", film.getName());
//        log.trace("Полные данные пользователя: {}", film);

        film.setId(getNextId());
        films.put(film.getId(), film);

//        log.debug("Фильм с названием {} успешно создан с ID: {}", film.getName(), film.getId());
        return film;
    }

    public Film update(Film newFilm) {
//        log.debug("Обновление фильма с ID: {}", newFilm.getId());
//        log.trace("Полные данные фильма для обновления: {}", newFilm);

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setDuration(newFilm.getDuration());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());

//            log.info("Фильм с ID {} успешно обновлен", newFilm.getId());
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
