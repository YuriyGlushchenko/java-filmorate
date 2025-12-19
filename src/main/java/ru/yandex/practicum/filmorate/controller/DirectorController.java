package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@Validated
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public Collection<Director> findAll() {
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public Director findDirectorById(@PathVariable int id) {
        return directorService.getDirectorById(id);
    }

    @PostMapping
    @Validated({Marker.OnCreate.class})
    public Director create(@Valid @RequestBody Director director) {
        return directorService.create(director);
    }

    @PutMapping
    @Validated({Marker.OnUpdate.class})
    public Director update(@RequestBody @Valid Director director) {
        return directorService.update(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        directorService.delete(id);
    }

}
