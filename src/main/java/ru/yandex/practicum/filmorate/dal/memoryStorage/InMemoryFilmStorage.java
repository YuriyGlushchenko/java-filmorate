package ru.yandex.practicum.filmorate.dal.memoryStorage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SortOrder;

import java.util.*;


@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();

    public Collection<Film> findAll() {
        return films.values();
    }

    public Film create(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);

        return film;
    }

    public Film update(Film newFilm) {
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setDuration(newFilm.getDuration());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());

            return oldFilm;
        }
        throw new NotFoundException("Фильма с id = " + newFilm.getId() + " не найдено");
    }

    public Optional<Film> getFilmById(int id) {
        return Optional.ofNullable(films.get(id));
    }

    private Integer getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @Override
    public void addLike(int filmId, int userId) {
        films.get(filmId).getLikesUserIds().add(userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        films.get(filmId).getLikesUserIds().remove(userId);
    }

    @Override
    public Collection<Film> findMostPopular(int count) {
        Comparator<Film> likesCountComparator = Comparator.comparingInt((Film f) -> f.getLikesUserIds().size()).reversed();

        return films.values().stream().sorted(likesCountComparator).limit(count).toList();
    }

    @Override
    public Collection<Film> findByDirectorId(int directorId, SortOrder sortOrder) {
        return List.of();
    }
}
