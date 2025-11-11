package ru.yandex.practicum.filmorate.service;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.GenreStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Comparator;

@Validated
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;


    public Collection<Genre> findAll() {
        return genreStorage.findAll();
    }


    public Genre getFilmById(int id) {
        return genreStorage.getGenreById(id).orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден"));
    }
//
//    public Collection<Film> findMostLikedFilms(
//            @Positive(message = "Количество фильмов для отображения должно быть положительным числом") int count) {
//
//        Comparator<Film> likesCountComparator = Comparator.comparingInt((Film f) -> f.getLikesUserIds().size()).reversed();
//
//        return filmRepository.findAll().stream().sorted(likesCountComparator).limit(count).toList();
//    }
//
//    public void addLike(int filmId, int userId) {
//        getValidatedLikedFilm(filmId, userId).getLikesUserIds().add(userId);
//    }
//
//    public void removeLike(int filmId, int userId) {
//        getValidatedLikedFilm(filmId, userId).getLikesUserIds().remove(userId);
//    }
//
//    private Film getValidatedLikedFilm(int filmId, int userId) {
//        userRepository.getUserById(userId)
//                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
//
//        return filmRepository.getFilmById(filmId)
//                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
//    }

}
