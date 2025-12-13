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

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
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

    // Обновила метод
    public Collection<Film> findMostPopularFilms(
            @Positive(message = "Количество фильмов для отображения должно быть положительным числом") int count,
            Integer genreId,
            Integer year) {

        validatePopularFilmsParameters(count, genreId, year);

        return filmRepository.findMostPopularWithFilters(count, genreId, year);
    }

    // Валидация параметров
    private void validatePopularFilmsParameters(int count, Integer genreId, Integer year) {
        if (count <= 0) {
            throw new ConditionsNotMetException("Параметр count должен быть положительным числом");
        }

        if (genreId != null && genreId <= 0) {
            throw new ConditionsNotMetException("ID жанра должен быть положительным числом");
        }

        // Проверка существования жанра, если указан
        if (genreId != null) {
            genreRepository.getGenreById(genreId)
                    .orElseThrow(() -> new NotFoundException("Жанр с id = " + genreId + " не найден"));
        }

        // Валидация года
        if (year != null) {
            int currentYear = LocalDate.now().getYear();
            if (year < 1895) { // Первый фильм был в 1895
                throw new ConditionsNotMetException("Год не может быть раньше 1895");
            }
        }
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

    // Для сортировки фильмов по кол-ву лайков
    private Comparator<Film> getLikesComparator() {
        return (film1, film2) -> {
            int likes1 = film1.getLikesCount() != null ? film1.getLikesCount() : film1.getLikesUserIds().size();
            int likes2 = film2.getLikesCount() != null ? film2.getLikesCount() : film2.getLikesUserIds().size();
            return Integer.compare(likes2, likes1);
        };
    }

}
