package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@Slf4j
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;


    @GetMapping
    public Collection<User> findAll() {
        log.info("GET /users: Запрос на получение всех пользователей");

        // сейчас UserController взаимодействует только с userService, а оттуда вызов просто прокидывается в UserStorage
        // Так правильно? или правильнее добавить сюда зависимость от UserStorage и вызывать его методы напрямую?
        return userService.findAll();
    }


    @PostMapping
    @Validated({Marker.OnCreate.class})
    public User create(@Valid @RequestBody User user) {
        log.info("POST /users: Создание пользователя с логином {}", user.getLogin());
        log.trace("Полные данные пользователя: {}", user);

        userService.create(user);

        return user;
    }

    @PutMapping
    @Validated({Marker.OnUpdate.class})
    public User update(@RequestBody @Valid User newUser) {
        log.debug("Обновление пользователя с ID: {}", newUser.getId());
        log.trace("Полные данные пользователя для обновления: {}", newUser);

        return userService.update(newUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addToFriends(@PathVariable("id") int userId, @PathVariable int friendId) {
        userService.addToFriends(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFromFriends(@PathVariable("id") int userId, @PathVariable int friendId) {
        userService.removeFromFriends(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getUserFriends(@PathVariable("id") int userId) {
        return userService.getUserFriends(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getMutualFriends(@PathVariable int id, @PathVariable int otherId) {
        return userService.getMutualFriends(id, otherId);
    }

}
