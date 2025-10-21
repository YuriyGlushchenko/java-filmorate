package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

@RequiredArgsConstructor
@Service
public class FilmService {
    private final FilmStorage storage;
}
