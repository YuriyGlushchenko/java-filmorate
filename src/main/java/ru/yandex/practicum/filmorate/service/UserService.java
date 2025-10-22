package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> findAll() {
        return userStorage.getAllUsers();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }


    public void addToFriends(int userId, int friendId) {
        correctUserPair(userId, friendId)
                .forEach(user -> user.getFriends().add(userId == user.getId() ? friendId : userId));
    }

    public void removeFromFriends(int userId, int friendId) {
        correctUserPair(userId, friendId)
                .forEach(user -> user.getFriends().remove(userId == user.getId() ? friendId : userId));
    }

    public List<User> getUserFriends(int userId) {
        User user = userStorage
                .getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + "не найден"));

        return user
                .getFriends()
                .stream()
                .map(id -> userStorage.getUserById(id).get())
                .collect(Collectors.toList());
    }

    public List<User> getMutualFriends(int userA, int userB) {
        List<User> userPair = correctUserPair(userA, userB);

        return userPair
                .getFirst()
                .getFriends()
                .stream()
                .filter(id -> userPair.getLast().getFriends().contains(id))
                .map(id -> userStorage
                        .getUserById(id)
                        .orElseThrow(() -> new NotFoundException("В списке друзей несуществующий пользователь с id = " + id)))
                .collect(Collectors.toList());
    }

    private List<User> correctUserPair(int firstUserId, int secondUserId) {
        Optional<User> optionalFirstUser = userStorage.getUserById(firstUserId);
        Optional<User> optionalSecondUser = userStorage.getUserById(secondUserId);

        if (optionalFirstUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + firstUserId + "не найден");
        }
        if (optionalSecondUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + secondUserId + "не найден");
        }

        return List.of(optionalFirstUser.get(), optionalSecondUser.get());
    }

}
