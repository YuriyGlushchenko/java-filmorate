package ru.yandex.practicum.filmorate.service;

import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.exceptions.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.*;
import java.util.stream.Collectors;

@Validated
@Service
public class FilmService {
    private final FilmStorage filmRepository;
    private final UserStorage userRepository;
    private final MpaRatingStorage mpaRatingRepository;
    private final GenreStorage genreRepository;
    private final DirectorStorage directorRepository;

    // Вместо @Qualifier выбираем конкретную реализацию бинов в файле настроек. Используется SpEL.
    @Autowired
    public FilmService(
            @Value("#{@${filmorate-app.storage.user-repository}}") UserStorage userRepository,
            @Value("#{@${filmorate-app.storage.film-repository}}") FilmStorage filmRepository,
            MpaRatingStorage mpaRatingRepository,
            GenreStorage genreRepository,
            DirectorStorage directorRepository) {
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
        this.mpaRatingRepository = mpaRatingRepository;
        this.genreRepository = genreRepository;
        this.directorRepository = directorRepository;
    }

    public Collection<Film> findAll() {
        Collection<Film> films = filmRepository.findAll();

        // Для каждого фильма загружаем режиссеров
        loadDirectorsForFilms(films);
        return films;
    }

    public Film create(Film newFilm) {
        // Приходит film без id, в поле mpa только id
        MpaRating rating = mpaRatingRepository.getMpaRatingById(newFilm.getMpa().getId())
                .orElseThrow(() -> new ConditionsNotMetException("Указанный рейтинг не найден"));

        // добавляем полноценный mpa из базы с id и названием
        newFilm.setMpa(rating);

        // проверяем жанры фильма на существование в базе
        validateGenres(newFilm.getGenres());

        // проверяем режиссеров фильма на существование в базе
        validateDirectors(newFilm.getDirectors());

        // сохраняем фильм в базу, получаем его id
        Film film = filmRepository.create(newFilm);

        // сохраняем все жанры фильма
        if (newFilm.getGenres() == null) {
            film.setGenres(new HashSet<>());
        } else {
            genreRepository.saveFilmGenres(film.getId(), film.getGenres());
        }

        // сохраняем всех режиссеров фильма
        if (newFilm.getDirectors() == null) {
            film.setDirectors(new HashSet<>());
        } else {
            directorRepository.saveFilmDirectors(film.getId(), film.getDirectors());
        }

        // загружаем полные данные режиссеров
        film.setDirectors(directorRepository.getFilmDirectorsByFilmId(film.getId()));

        return film;
    }

    public Film update(Film newFilm) {
        filmRepository.getFilmById(newFilm.getId())
                .orElseThrow(() -> new NotFoundException("Данные не обновлены. Фильм с id=" + newFilm.getId() + " не найден"));

        // проверяем жанры фильма на существование в базе
        validateGenres(newFilm.getGenres());

        // проверяем режиссеров фильма на существование в базе
        validateDirectors(newFilm.getDirectors());

        // сохраняем все жанры фильма
        if (newFilm.getGenres() == null) {
            newFilm.setGenres(new HashSet<>());
        } else {
            genreRepository.saveFilmGenres(newFilm.getId(), newFilm.getGenres());
        }

        // сохраняем всех режиссеров фильма
        if (newFilm.getDirectors() == null) {
            newFilm.setDirectors(new HashSet<>());
        } else {
            directorRepository.saveFilmDirectors(newFilm.getId(), newFilm.getDirectors());
        }

        Film updatedFilm = filmRepository.update(newFilm);

        // загружаем полные данные режиссеров для обновленного фильма
        updatedFilm.setDirectors(directorRepository.getFilmDirectorsByFilmId(updatedFilm.getId()));

        return updatedFilm;
    }

    public Film getFilmById(int id) {
        Film film = filmRepository.getFilmById(id).orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));

        // Загружаем жанры
        Set<Genre> genres = new HashSet<>(genreRepository.getFilmGenresByFilmId(id));
        film.setGenres(genres);

        // Загружаем режиссеров
        Set<Director> directors = new HashSet<>(directorRepository.getFilmDirectorsByFilmId(id));
        film.setDirectors(directors);

        return film;
    }

    public Collection<Film> findMostPopularFilms(
            @Positive(message = "Количество фильмов для отображения должно быть положительным числом") int count) {

        Collection<Film> films = filmRepository.findMostPopular(count);
        // Загружаем режиссеров для популярных фильмов
        loadDirectorsForFilms(films);
        return films;
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

    private void validateDirectors(Set<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return;
        }

        Set<Integer> existingDirectorIds = directorRepository.findAll().stream()
                .map(Director::getId)
                .collect(Collectors.toSet());

        for (Director director : directors) {
            if (!existingDirectorIds.contains(director.getId())) {
                throw new ConditionsNotMetException("Режиссер с id = " + director.getId() + " не найден");
            }
        }
    }

    // Вспомогательный метод для загрузки режиссеров для коллекции фильмов
    private void loadDirectorsForFilms(Collection<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }

        // Получаем ID всех фильмов
        Set<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        // Пакетный запрос (один запрос сразу для всех требуемых фильмов, чтобы не в цикле перебирать)
        Map<Integer, Set<Director>> directorsByFilmId = directorRepository.getFilmDirectorsForFilms(filmIds);

        // Перебираем фильмы и назначаем режиссёров
        for (Film film : films) {
            Set<Director> directors = directorsByFilmId.getOrDefault(film.getId(), new HashSet<>());
            film.setDirectors(directors);
        }
    }
}