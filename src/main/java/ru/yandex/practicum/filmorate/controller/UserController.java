package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("GET /users: Запрос на получение всех пользователей");
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("POST /users: Создание пользователя с логином {}", user.getLogin());
        log.trace("Полные данные пользователя: {}", user);

        if (!isEmailCorrect(user.getEmail())
                || !isLoginCorrect(user.getLogin())
                || user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Некорректные данные: {}, {}, {}", user.getEmail(), user.getLogin(), user.getBirthday());
            throw new ValidationException("Параметры пользователя недопустимы");
        }

        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Пользователю с именем ->|{}|<- присвоено имя {}", user.getName(), user.getLogin());
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);
        log.debug("Пользователь с логином {} успешно создан с ID: {}",user.getLogin(), user.getId());

        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.debug("Обновление пользователя с ID: {}", newUser.getId());
        log.trace("Полные данные пользователя для обновления: {}", newUser);

        if (newUser.getId() <= 0) {
            log.warn("Передан пользователь с некорректным id {}", newUser.getId());
            throw new ValidationException("Id должен быть корректно указан (положительное целое число)");
        }

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());


            if (isEmailCorrect(newUser.getEmail())) {
                oldUser.setEmail(newUser.getEmail());
                log.debug("Обновлена почта: {}", newUser.getEmail());
            }
            if (isLoginCorrect(newUser.getLogin())) {
                oldUser.setLogin(newUser.getLogin());
                log.debug("Обновлен логин: {}", newUser.getLogin());
            }
            if (newUser.getBirthday().isBefore(LocalDate.now())) {
                oldUser.setBirthday(newUser.getBirthday());
                log.debug("Обновлена дата рождения: {}", newUser.getBirthday());
            }
            if (newUser.getName() != null && !newUser.getName().isBlank()) {
                oldUser.setName(newUser.getName());
                log.debug("Обновлено имя пользователя: {}", newUser.getName());
            }

            log.info("Пользователь с ID {} успешно обновлен", newUser.getId());
            return oldUser;
        }

        log.error("Пользователь с ID {} не найден", newUser.getId());
        throw new NotFoundException("Пользователя с id = " + newUser.getId() + " не найдено");
    }

    private int getNextId() {
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
