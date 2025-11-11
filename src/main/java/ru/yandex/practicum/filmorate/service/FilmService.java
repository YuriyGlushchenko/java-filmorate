package ru.yandex.practicum.filmorate.service;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.GenreStorage;
import ru.yandex.practicum.filmorate.dal.MpaRatingStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.*;

@Validated
@Service
@RequiredArgsConstructor
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
    }

    public Film create(Film film) {
        // toDo дописать сохранение жанров в genreRepository методом saveFilmGenres
        return filmRepository.create(film);
    }

    public Film update(Film newFilm) {
        return filmRepository.update(newFilm);
    }

    public Film getFilmById(int id) {
        Film film =  filmRepository.getFilmById(id).orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));

        MpaRating rating = mpaRatingRepository.getMpaRatingById(film.getMpa().getId()).orElseThrow(
                ()-> new RuntimeException("Категория фильма не найдена")
        );

        Set<Genre> genres = new HashSet<>(genreRepository.getFilmGenresByFilmId(id));

        film.setMpa(rating);
        film.setGenres(genres);

        return film;
    }

    public Collection<Film> findMostLikedFilms(
            @Positive(message = "Количество фильмов для отображения должно быть положительным числом") int count) {

        Comparator<Film> likesCountComparator = Comparator.comparingInt((Film f) -> f.getLikesUserIds().size()).reversed();

        return filmRepository.findAll().stream().sorted(likesCountComparator).limit(count).toList();
    }

    public void addLike(int filmId, int userId) {
        getValidatedLikedFilm(filmId, userId).getLikesUserIds().add(userId);
    }

    public void removeLike(int filmId, int userId) {
        getValidatedLikedFilm(filmId, userId).getLikesUserIds().remove(userId);
    }

    private Film getValidatedLikedFilm(int filmId, int userId) {
        userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        return filmRepository.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
    }

}
