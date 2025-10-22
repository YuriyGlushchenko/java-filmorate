package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public void addToFriends(int userId, int friendId) {
        correctUserPair(userId, friendId)
                .forEach(user -> user.getFriends().add(userId == user.getId() ? friendId : userId));
    }

    private List<User> correctUserPair(int firstUserId, int secondUserId) {
        Optional<User> optionalFirstUser = userStorage.getUserById(firstUserId);
        Optional<User> optionalSeconUser = userStorage.getUserById(secondUserId);

        if (optionalFirstUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + firstUserId + "не найден");
        }
        if (optionalSeconUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + secondUserId + "не найден");
        }

        return List.of(optionalFirstUser.get(), optionalSeconUser.get());
    }

}
