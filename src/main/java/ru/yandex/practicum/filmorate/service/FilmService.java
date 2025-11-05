package ru.yandex.practicum.filmorate.service;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;

@Validated
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id).orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    public Collection<Film> findMostLikedFilms(
            @Positive(message = "Количество фильмов для отображения должно быть положительным числом") int count) {

        Comparator<Film> likesCountComparator = Comparator.comparingInt((Film f) -> f.getLikesUserIds().size()).reversed();

        return filmStorage.findAll().stream().sorted(likesCountComparator).limit(count).toList();
    }

    public void addLike(int filmId, int userId) {
        getValidatedLikedFilm(filmId, userId).getLikesUserIds().add(userId);
    }

    public void removeLike(int filmId, int userId) {
        getValidatedLikedFilm(filmId, userId).getLikesUserIds().remove(userId);
    }

    private Film getValidatedLikedFilm(int filmId, int userId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        return filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
    }

}
