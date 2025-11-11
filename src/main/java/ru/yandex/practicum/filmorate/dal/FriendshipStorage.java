package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface FriendshipStorage {

    Optional<Friendship> getFriendshipByUsersIds(int userId, int friendId);

    Friendship create(Friendship friendship);

    List<User> getUserFriends(int userId);

    void removeFromFriends(int userId, int friendId);

    List<User> findCommonFriends(int userA, int userB);

//     List<User> getUserFriends(int userId);
//
//    User update(User newUser);
//
//    Optional<User> getUserById(int id);
//
//    Optional<User> getUserByLogin(String email);
//
//    Optional<User> findDuplicateUser(String email, String login);
//
//    void removeFromFriends(int userId, int friendId);

}
