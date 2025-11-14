package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FriendshipStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.dto.UserDTO;
import ru.yandex.practicum.filmorate.exceptions.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userRepository;
    private final FriendshipStorage friendshipRepository;

    // Вместо @Qualifier и хардкода выбираем конкретную реализацию бинов в файле настроек. Используется SpEL.
    // *{} - Spring Expression Language, внутри @имя_бина, ${} - значение из application.yml.
    @Autowired
    public UserService(@Value("#{@${filmorate-app.storage.user-repository}}") UserStorage userRepository,
                       FriendshipStorage friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

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
        User user = userRepository.getUserById(updateUser.getId())
                .orElseThrow(() -> new NotFoundException("Данные не обновлены. Пользователь с id=" + updateUser.getId() + " не найден"));

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

    public User getUserById(int id) {
        User user = userRepository.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));

        List<Integer> friendsIds = friendshipRepository.getUserFriends(id).stream()
                .map(User::getId)
                .toList();

        user.getFriends().clear();
        user.getFriends().addAll(friendsIds);

        return user;
    }

}
