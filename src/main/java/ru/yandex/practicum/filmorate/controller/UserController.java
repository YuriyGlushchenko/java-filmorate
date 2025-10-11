package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
@Validated
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("GET /users: Запрос на получение всех пользователей");
        return users.values();
    }


    @PostMapping
    @Validated({Marker.OnCreate.class})
    public User create(@Valid @RequestBody User user) {
        log.info("POST /users: Создание пользователя с логином {}", user.getLogin());
        log.trace("Полные данные пользователя: {}", user);

        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Пользователю с именем ->|{}|<- присвоено имя {}", user.getName(), user.getLogin());
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);
        log.debug("Пользователь с логином {} успешно создан с ID: {}", user.getLogin(), user.getId());

        return user;
    }

    @PutMapping
    @Validated({Marker.OnUpdate.class})
    public User update(@RequestBody @Valid User newUser) {
        log.debug("Обновление пользователя с ID: {}", newUser.getId());
        log.trace("Полные данные пользователя для обновления: {}", newUser);

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            oldUser.setEmail(newUser.getEmail());
            oldUser.setLogin(newUser.getLogin());
            oldUser.setBirthday(newUser.getBirthday());

            if (newUser.getName() != null && !newUser.getName().isBlank()) {
                oldUser.setName(newUser.getName());
                log.debug("Обновлено имя пользователя на новое имя: {}", newUser.getName());
            } else {
                oldUser.setName(newUser.getLogin());
                log.debug("Обновлено имя пользователя: вместо имени {} установлено имя {}", newUser.getName(), newUser.getLogin());
            }

            log.info("Пользователь с ID {} успешно обновлен", newUser.getId());
            return oldUser;
        }

        throw new NotFoundException("Пользователя с id = " + newUser.getId() + " не найдено");
    }

    private Integer getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private boolean isEmailCorrect(String email) {
        return (email != null && email.contains("@"));
    }

    private boolean isLoginCorrect(String login) {
        return (login != null && !login.matches(".*\\s.*") && !login.isBlank());
    }
}
