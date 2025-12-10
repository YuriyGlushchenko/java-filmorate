package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface DirectorStorage {
    Collection<Director> findAll();

    Optional<Director> getDirectorById(int id);

    Director create(Director director);

    Director update(Director newDirector);

    void delete(int id);

    void saveFilmDirectors(int filmId, Set<Director> directors);

    void deleteFilmDirectors(int filmId);

    Set<Director> getFilmDirectorsByFilmId(int filmId);

    Map<Integer, Set<Director>> getFilmDirectorsForFilms(Collection<Integer> filmIds);
}
