package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.dal.GenreStorage;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Validated
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;


    public Collection<Genre> findAll() {
        return genreStorage.findAll();
    }


    public Genre getGenreById(int id) {
        return genreStorage.getGenreById(id).orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден"));
    }
}
