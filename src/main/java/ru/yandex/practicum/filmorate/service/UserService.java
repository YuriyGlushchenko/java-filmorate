package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.dto.UserDTO;
import ru.yandex.practicum.filmorate.exceptions.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userRepository;
    private final FriendshipService friendshipService;
    private final FilmStorage filmRepository;
    private final FilmService filmService;

    public Collection<UserDTO> getAllUsers() {
        return userRepository.getAllUsers().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public User create(User user) {
        Optional<User> alreadyExistUser = userRepository.findDuplicateDataUser(user.getEmail(), user.getLogin());

        if (alreadyExistUser.isPresent()) {
            User existUser = alreadyExistUser.get();

            if (existUser.getLogin().equals(user.getLogin()) && existUser.getEmail().equals(user.getEmail())) {
                throw new DuplicatedDataException("Пользователь с таким Login и Email уже зарегистрирован");
            } else if (existUser.getLogin().equals(user.getLogin())) {
                throw new DuplicatedDataException("Пользователь с таким Login уже зарегистрирован");
            }
            throw new DuplicatedDataException("Пользователь с таким Email уже зарегистрирован");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            log.trace("Пользователю с именем ->|{}|<- присвоено имя {}", user.getName(), user.getLogin());
            user.setName(user.getLogin());
        }

        return userRepository.create(user);
    }

    public User update(User updateUser) {
        User user = validateUser(updateUser.getId());

        Optional<User> alreadyExistUser = userRepository.findDuplicateDataUser(updateUser.getEmail(), updateUser.getLogin());

        if (alreadyExistUser.isPresent()) {
            if (alreadyExistUser.get().getId() != updateUser.getId()) {
                throw new DuplicatedDataException("Такой Login или Email уже используется");
            }
        }
        if (updateUser.getName() == null || updateUser.getName().isBlank()) {
            log.trace("Имя пользователя не обновлено: |{}|", user.getName());
            updateUser.setName(user.getName());
        }

        return userRepository.update(updateUser);
    }

    public void delete(int userId) {
        validateUser(userId);

        userRepository.delete(userId);
    }

    public User getUserById(int userId) {
        return validateUser(userId);
    }

    public void addToFriends(int userId, int friendId) {
        friendshipService.addToFriends(userId, friendId);
    }

    public void removeFromFriends(int userId, int friendId) {
        friendshipService.removeFromFriends(userId, friendId);
    }

    public Collection<UserDTO> getUserFriends(int userId) {
        return friendshipService.getUserFriends(userId);
    }

    public Collection<User> findCommonFriends(int id, int otherId) {
        return friendshipService.findCommonFriends(id, otherId);
    }

    public Collection<Film> getRecommendations(int userId) {
        validateUser(userId);

        return filmService.getRecommendations(userId);
    }

    private User validateUser(int userId) {
        return userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Операция не выполнена. Пользователь с id=" + userId + " не найден"));
    }
}
