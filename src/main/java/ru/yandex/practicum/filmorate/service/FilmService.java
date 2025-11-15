package ru.yandex.practicum.filmorate.service;

import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.GenreStorage;
import ru.yandex.practicum.filmorate.dal.MpaRatingStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exceptions.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Validated
@Service
public class FilmService {
    private final FilmStorage filmRepository;
    private final UserStorage userRepository;
    private final MpaRatingStorage mpaRatingRepository;
    private final GenreStorage genreRepository;

    // Вместо @Qualifier выбираем конкретную реализацию бинов в файле настроек. Используется SpEL.
    @Autowired
    public FilmService(
            @Value("#{@${filmorate-app.storage.user-repository}}") UserStorage userRepository,
            @Value("#{@${filmorate-app.storage.film-repository}}") FilmStorage filmRepository,
            MpaRatingStorage mpaRatingRepository,
            GenreStorage genreRepository) {
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
        this.mpaRatingRepository = mpaRatingRepository;
        this.genreRepository = genreRepository;
    }

    public Collection<Film> findAll() {
        return filmRepository.findAll();

        // toDo добавить жанры
    }

    public Film create(Film newFilm) {
        // Приходит film без id, в поле mpa только id
        MpaRating rating = mpaRatingRepository.getMpaRatingById(newFilm.getMpa().getId())
                .orElseThrow(() -> new ConditionsNotMetException("Указанный рейтинг не найден"));

        // добавляем полноценный mpa из базы с id и названием
        newFilm.setMpa(rating);

        // сохраняем фильм в базу, получаем его id
        Film film = filmRepository.create(newFilm);

        // проверяем жанры фильма на существование в базе
        validateGenres(newFilm.getGenres());

        // сохраняем все жанры фильма
        if (newFilm.getGenres() == null) {
            film.setGenres(new HashSet<>());
        } else {
            genreRepository.saveFilmGenres(film.getId(), film.getGenres());
        }

        // возвращается полноценный film с присвоенным id, полноценным mpa
        return film;
    }

    public Film update(Film newFilm) {
        filmRepository.getFilmById(newFilm.getId())
                .orElseThrow(() -> new NotFoundException("Данные не обновлены. Фильм с id=" + newFilm.getId() + " не найден"));

        // проверяем жанры фильма на существование в базе
        validateGenres(newFilm.getGenres());

        // сохраняем все жанры фильма
        if (newFilm.getGenres() == null) {
            newFilm.setGenres(new HashSet<>());
        } else {
            genreRepository.saveFilmGenres(newFilm.getId(), newFilm.getGenres());
        }

        return filmRepository.update(newFilm);
    }

    public Film getFilmById(int id) {
        Film film = filmRepository.getFilmById(id).orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));

        Set<Genre> genres = new HashSet<>(genreRepository.getFilmGenresByFilmId(id));

        film.setGenres(genres);

        return film;
    }

    public Collection<Film> findMostPopularFilms(
            @Positive(message = "Количество фильмов для отображения должно быть положительным числом") int count) {

        return filmRepository.findMostPopular(count);
    }

    public void addLike(int filmId, int userId) {
        validateLikeFilmData(filmId, userId);

        filmRepository.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        validateLikeFilmData(filmId, userId);

        filmRepository.removeLike(filmId, userId);
    }

    private void validateLikeFilmData(int filmId, int userId) {
        userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        filmRepository.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
    }

    private void validateGenres(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        Set<Integer> existingGenreIds = genreRepository.findAll().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        for (Genre genre : genres) {
            if (!existingGenreIds.contains(genre.getId())) {
                throw new ConditionsNotMetException("Жанр с id = " + genre.getId() + " не найден");
            }
        }
    }

}
