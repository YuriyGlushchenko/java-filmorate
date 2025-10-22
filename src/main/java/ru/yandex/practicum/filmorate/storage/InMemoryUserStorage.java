package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.Valid;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage{
    private final Map<Integer, User> users = new HashMap<>();

    public Collection<User> getAllUsers() {
        return users.values();
    }

    public User create(User user) {
//        log.info("POST /users: Создание пользователя с логином {}", user.getLogin());
//        log.trace("Полные данные пользователя: {}", user);

        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
//            log.debug("Пользователю с именем ->|{}|<- присвоено имя {}", user.getName(), user.getLogin());
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);
//        log.debug("Пользователь с логином {} успешно создан с ID: {}", user.getLogin(), user.getId());

        return user;
    }


    public User update(User newUser) {
//        log.debug("Обновление пользователя с ID: {}", newUser.getId());
//        log.trace("Полные данные пользователя для обновления: {}", newUser);

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            oldUser.setEmail(newUser.getEmail());
            oldUser.setLogin(newUser.getLogin());
            oldUser.setBirthday(newUser.getBirthday());

            if (newUser.getName() != null && !newUser.getName().isBlank()) {
                oldUser.setName(newUser.getName());
//                log.debug("Обновлено имя пользователя на новое имя: {}", newUser.getName());
            } else {
                oldUser.setName(newUser.getLogin());
//                log.debug("Обновлено имя пользователя: вместо имени {} установлено имя {}", newUser.getName(), newUser.getLogin());
            }

//            log.info("Пользователь с ID {} успешно обновлен", newUser.getId());
            return oldUser;
        }

        throw new NotFoundException("Пользователя с id = " + newUser.getId() + " не найдено");
    }




    public Optional<User> getUserById(int id){
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
