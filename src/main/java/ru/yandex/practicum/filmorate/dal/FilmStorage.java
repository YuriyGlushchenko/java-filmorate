package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SortOrder;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film newFilm);

    Optional<Film> getFilmById(int id);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    Collection<Film> findMostPopular(int count);

    Collection<Film> findByDirectorId(int directorId, SortOrder sortOrder);

    void delete(int id);

    Collection<Film> getRecomendations(int userId);

    Collection<Film> getCommonFilms(int userId, int friendId);
}
