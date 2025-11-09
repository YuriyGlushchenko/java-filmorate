package ru.yandex.practicum.filmorate.dal.dBStorage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository("UserDbRepository")
public class UserDbRepository implements UserStorage {
    @Override
    public Collection<User> getAllUsers() {
        return List.of();
    }

    @Override
    public User create(User user) {
        return null;
    }

    @Override
    public User update(User newUser) {
        return null;
    }

    @Override
    public Optional<User> getUserById(int id) {
        return Optional.empty();
    }
}
