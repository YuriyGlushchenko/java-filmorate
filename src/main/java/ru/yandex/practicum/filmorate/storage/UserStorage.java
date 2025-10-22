package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Optional;

public interface UserStorage {
    Optional<User> getUserById(int id);
}
