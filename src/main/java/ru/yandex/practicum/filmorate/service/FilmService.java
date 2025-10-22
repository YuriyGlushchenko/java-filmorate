package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private final Comparator<Film> likesCountComparator = Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed();

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(@RequestBody @Valid Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public Collection<Film> findMostLikedFilms(int count) {
        return filmStorage.findAll().stream().sorted(likesCountComparator).limit(count).toList();
    }

    public void addLike(int filmId, int userId) {
        Optional<Film> optionalFilm = filmStorage.getFilmById(filmId);
        if (optionalFilm.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + "не найден");
        }

        Optional<User> optionalUser = userStorage.getUserById(userId);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + "не найден");
        }

        optionalFilm.get().getLikes().add(userId);
    }

    public void removeLike(int filmId, int userId) {
        Optional<Film> optionalFilm = filmStorage.getFilmById(filmId);

        optionalFilm.orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + "не найден")).getLikes().remove(userId);
    }
}
