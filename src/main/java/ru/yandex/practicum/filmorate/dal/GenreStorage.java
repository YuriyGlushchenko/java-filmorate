package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

public interface GenreStorage {

    List<Genre> findAll();

    Optional<Genre> getGenreById(int id);

    List<Genre> getFilmGenresByFilmId(int filmId);

    void saveFilmGenres(int filmId, Set<Genre> genres);

    Map<Integer, Set<Genre>> getFilmGenresForFilms(Collection<Integer> filmIds);
}
