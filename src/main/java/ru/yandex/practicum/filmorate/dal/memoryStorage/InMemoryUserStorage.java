package ru.yandex.practicum.filmorate.dal.memoryStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();

    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return users.values()
                .stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<User> getUserByLogin(String login) {
        return users.values()
                .stream()
                .filter(u -> u.getLogin().equals(login))
                .findFirst();
    }

    public User create(User user) {
        user.setId(getNextId());

        users.put(user.getId(), user);

        return user;
    }

    public User update(User newUser) {
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            oldUser.setEmail(newUser.getEmail());
            oldUser.setLogin(newUser.getLogin());
            oldUser.setBirthday(newUser.getBirthday());

            if (newUser.getName() != null && !newUser.getName().isBlank()) {
                oldUser.setName(newUser.getName());
                log.trace("Обновлено имя пользователя на новое имя: {}", newUser.getName());
            } else {
                oldUser.setName(newUser.getLogin());
                log.trace("Обновлено имя пользователя: вместо имени {} установлено имя {}", newUser.getName(), newUser.getLogin());
            }

            return oldUser;
        }

        throw new NotFoundException("Пользователя с id = " + newUser.getId() + " не найдено");
    }

    public Optional<User> getUserById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    private Integer getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
