package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

public interface DirectorStorage {
    Collection<Director> findAll();

    Optional<Director> getDirectorById(int id);

    Director create(Director director);

    Director update(Director newDirector);

    void delete(int id);
}
