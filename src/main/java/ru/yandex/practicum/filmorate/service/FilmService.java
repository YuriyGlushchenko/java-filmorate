package ru.yandex.practicum.filmorate.service;

import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.exceptions.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

        // Подгружаем жанры для всех фильмов
        loadGenresForFilms(films);

        // Подгружаем режиссёров для всех фильмов
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

        // сохраняем все связи жанров с фильмом
        if (newFilm.getGenres() == null) {
            film.setGenres(new HashSet<>());
        } else {
            genreRepository.saveFilmGenres(film.getId(), film.getGenres());
        }

        // сохраняем все связи режиссеров с фильмом
        if (newFilm.getDirectors() == null) {
            film.setDirectors(new HashSet<>());
        } else {
            directorRepository.saveFilmDirectors(film.getId(), film.getDirectors());
        }

        return film;
    }

    public Film update(Film newFilm) {
        filmRepository.getFilmById(newFilm.getId())
                .orElseThrow(() -> new NotFoundException("Данные не обновлены. Фильм с id=" + newFilm.getId() + " не найден"));

        // проверяем жанры фильма на существование в базе
        validateGenres(newFilm.getGenres());

        // проверяем режиссеров фильма на существование в базе
        validateDirectors(newFilm.getDirectors());

        // сохраняем все связи жанров с фильмом
        if (newFilm.getGenres() == null) {
            newFilm.setGenres(new HashSet<>());
        } else {
            genreRepository.saveFilmGenres(newFilm.getId(), newFilm.getGenres());
        }

        // сохраняем все связи режиссеров с фильмом
        if (newFilm.getDirectors() == null) {
            newFilm.setDirectors(new HashSet<>());
        } else {
            directorRepository.saveFilmDirectors(newFilm.getId(), newFilm.getDirectors());
        }

        return filmRepository.update(newFilm);
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
        // Загружаем жанры и режиссеров для популярных фильмов
        loadGenresForFilms(films);
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

    public Collection<Film> findByDirectorId(int directorId, SortOrder sortOrder) {
        // Сначала проверяем что режиссер вообще существует
        directorRepository.getDirectorById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссер с id = " + directorId + " не найден"));

        // Получаем фильмы с сортировкой
        Collection<Film> films = filmRepository.findByDirectorId(
                directorId,
                sortOrder
        );

        // Загружаем жанры и режиссеров для найденных фильмов
        loadGenresForFilms(films);
        loadDirectorsForFilms(films);

        return films;
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

    // Вспомогательный метод для загрузки жанров для коллекции фильмов
    private void loadGenresForFilms(Collection<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }

        // Получаем ID всех фильмов
        Set<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        // Пакетный запрос (один запрос сразу для всех требуемых фильмов)
        Map<Integer, Set<Genre>> genresByFilmId = genreRepository.getFilmGenresForFilms(filmIds);

        // Перебираем фильмы и назначаем жанры
        for (Film film : films) {
            Set<Genre> genres = genresByFilmId.getOrDefault(film.getId(), new HashSet<>());
            film.setGenres(genres);
        }
    }
}