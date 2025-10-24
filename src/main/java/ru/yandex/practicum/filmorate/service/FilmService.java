package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;

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

    public Collection<Film> findMostLikedFilms(int count) {
        if(count < 0){
            throw new ValidationException("count", count, "Количество фильмов для отображения не может быть отрицательным");
        }
        Comparator<Film> likesCountComparator = Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed();

        return filmStorage.findAll().stream().sorted(likesCountComparator).limit(count).toList();
    }

    public void addLike(int filmId, int userId) {
        getValidatedLikedFilm(filmId, userId).getLikes().add(userId);
    }

    public void removeLike(int filmId, int userId) {
        getValidatedLikedFilm(filmId, userId).getLikes().remove(userId);
    }

    private Film getValidatedLikedFilm(int filmId, int userId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        return filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
    }

}
