package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FriendshipStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.dto.FriendDTO;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FriendMapper;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
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

        List<User> userPair = correctUserPair(userId, friendId);

        Friendship friendship = Friendship.builder()
                .userId(userId)
                .friendId(friendId)
                .status(FriendshipStatus.PENDING)
                .build();

        return friendshipRepository.create(friendship);

    }

        public void removeFromFriends(int userId, int friendId) {
            List<User> userPair = correctUserPair(userId, friendId);

            friendshipRepository.removeFromFriends(userId,friendId);


    }

    public List<FriendDTO> getUserFriends(int userId) {
        Optional<User> userOptional = userRepository.getUserById(userId);

        if(userOptional.isEmpty()){
            log.debug("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Данные не обновлены. Пользователь с id=" + userId + " не найден");
        }


        List<User> friends = friendshipRepository.getUserFriends(userId);
        return friends.stream()
                .map(FriendMapper::mapToFriendDto)
                .toList();
    }

    public List<User> getMutualFriends(int userA, int userB) {
        List<User> userPair = correctUserPair(userA, userB);

        friendshipRepository.getMutualFriends(userA, userB);

        return userPair;

    }

    private List<User> correctUserPair(int firstUserId, int secondUserId) {
        Optional<User> optionalFirstUser = userRepository.getUserById(firstUserId);
        Optional<User> optionalSecondUser = userRepository.getUserById(secondUserId);

        if (optionalFirstUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + firstUserId + "не найден");
        }
        if (optionalSecondUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + secondUserId + "не найден");
        }

        // ordered collection → "упорядоченная коллекция" (сохраняет порядок элементов)
        return List.of(optionalFirstUser.get(), optionalSecondUser.get());
    }
}
