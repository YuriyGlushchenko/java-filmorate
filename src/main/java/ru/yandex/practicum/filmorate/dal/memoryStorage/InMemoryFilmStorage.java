package ru.yandex.practicum.filmorate.dal.memoryStorage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
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
        Film film = films.get(filmId);
        if (film != null) {
            film.getLikesUserIds().add(userId);
        }
    }

    @Override
    public void removeLike(int filmId, int userId) {
        Film film = films.get(filmId);
        if (film != null) {
            film.getLikesUserIds().remove(userId);
        }
    }

    @Override
    public Collection<Film> findMostPopular(int count) {
        Comparator<Film> likesCountComparator = Comparator.comparingInt((Film f) -> f.getLikesUserIds().size()).reversed();
        return films.values().stream().sorted(likesCountComparator).limit(count).toList();
    }

    @Override
    public Collection<Film> findMostPopularWithFilters(int count, Integer genreId, Integer year) {
        Comparator<Film> likesCountComparator = (film1, film2) -> {
            int likes1 = film1.getLikesCount() != null ? film1.getLikesCount() : film1.getLikesUserIds().size();
            int likes2 = film2.getLikesCount() != null ? film2.getLikesCount() : film2.getLikesUserIds().size();
            return Integer.compare(likes2, likes1);
        };

        return films.values().stream()
                .filter(film -> {
                    if (genreId == null) return true;
                    Set<Genre> genres = film.getGenres();
                    if (genres == null || genres.isEmpty()) {
                        return false;
                    }
                    return genres.stream()
                            .anyMatch(genre -> genre.getId().equals(genreId));
                })
                .filter(film -> {
                    if (year == null) return true;
                    LocalDate releaseDate = film.getReleaseDate();
                    if (releaseDate == null) {
                        return false;
                    }
                    return releaseDate.getYear() == year;
                })
                .sorted(likesCountComparator)
                .limit(count)
                .toList();
    }
}