package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendshipStorage {

    Friendship create(Friendship friendship);

    List<User> getUserFriends(int userId);

    void removeFromFriends(int userId, int friendId);

    List<User> findCommonFriends(int userA, int userB);

}
