package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    Collection<User> getAllUsers();

    User create(User user);

    User update(User newUser);

    Optional<User> getUserById(int id);

    Optional<User> findDuplicateDataUser(String email, String login);

    List<Integer> checkUserIds(int[] userIds);

}
