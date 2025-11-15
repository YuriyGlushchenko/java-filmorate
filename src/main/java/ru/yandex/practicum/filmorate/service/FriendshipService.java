package ru.yandex.practicum.filmorate.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FriendshipStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.dto.UserDTO;
import ru.yandex.practicum.filmorate.exceptions.exceptions.InternalServerException;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Service
@Slf4j
//@RequiredArgsConstructor
public class FriendshipService {
    private final FriendshipStorage friendshipRepository;
    private final UserStorage userRepository;

    @Autowired
    public FriendshipService(
            @Value("#{@${filmorate-app.storage.user-repository}}") UserStorage userRepository,
            FriendshipStorage friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }


    public Friendship addToFriends(int userId, int friendId) {

        validateUsersExist(userId, friendId);

        Friendship friendship = Friendship.builder()
                .userId(userId)
                .friendId(friendId)
                .status(FriendshipStatus.PENDING)
                .build();

        return friendshipRepository.create(friendship);

    }

    public void removeFromFriends(int userId, int friendId) {
        validateUsersExist(userId, friendId);

        friendshipRepository.removeFromFriends(userId, friendId);


    }

    public List<UserDTO> getUserFriends(int userId) {
        validateUsersExist(userId);

        List<User> friends = friendshipRepository.getUserFriends(userId);
        return friends.stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public List<User> findCommonFriends(int userA, int userB) {
        validateUsersExist(userA, userB);

        return friendshipRepository.findCommonFriends(userA, userB);
    }

    private void validateUsersExist(int... userIds) {
        if (userIds == null || userIds.length == 0) {
            throw new InternalServerException("Список ID пользователей не может быть пустым");
        }

        List<Integer> correctIds = userRepository.checkUserIds(userIds);
        for (int userId : userIds) {

            if (!correctIds.contains(userId)) {
                throw new NotFoundException("Данные не обновлены. Пользователь с id=" + userId + " не найден");
            }
        }


    }
}
