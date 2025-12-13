package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film newFilm);

    Optional<Film> getFilmById(int id);

    void addLike(int filmId, int userId);

    public void removeLike(int filmId, int userId);

    Collection<Film> findMostPopular(int count);

    Collection<Film> findMostPopularWithFilters(int count, Integer genreId, Integer year);
}
