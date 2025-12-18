package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.dal.DirectorStorage;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

@Validated
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorRepository;

    public Collection<Director> findAll() {
        return directorRepository.findAll();
    }

    public Director getDirectorById(int id) {
        return directorRepository.getDirectorById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id = " + id + " не найден"));
    }

    public Director create(Director director) {
        return directorRepository.create(director);
    }

    public Director update(Director newDirector) {
        directorRepository.getDirectorById(newDirector.getId())
                .orElseThrow(() -> new NotFoundException("Данные не обновлены. Режиссёр с id=" + newDirector.getId() + " не найден"));

        return directorRepository.update(newDirector);
    }

    public void delete(int id) {
        directorRepository.getDirectorById(id)
                .orElseThrow(() -> new NotFoundException("Данные не удалены. Режиссёр с id=" + id + " не найден"));

        directorRepository.delete(id);
    }

}
