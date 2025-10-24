package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Validated
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @PostMapping
    @Validated({Marker.OnCreate.class})
    public Film create(@Valid @RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    @Validated({Marker.OnUpdate.class})
    public Film update(@RequestBody @Valid Film newFilm) {
        return filmService.update(newFilm);
    }

    @GetMapping("/{id}")
    public Film findFilmById(@PathVariable int id) {
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") int filmId, @PathVariable int userId) {
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable("id") int filmId, @PathVariable int userId) {
        filmService.removeLike(filmId, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> findMostLikedFilms(@RequestParam(defaultValue = "10") int count) {

        return filmService.findMostLikedFilms(count);
    }
}
