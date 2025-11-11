package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FriendshipStorage;
import ru.yandex.practicum.filmorate.dto.UserDTO;
import ru.yandex.practicum.filmorate.exceptions.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dal.UserStorage;

import java.util.Collection;
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
    public UserService(
            @Value("#{@${filmorate-app.storage.user-repository}}") UserStorage userRepository,
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

        if(alreadyExistUser.isPresent()){
            User existUser = alreadyExistUser.get();

            if(existUser.getLogin().equals(user.getLogin()) && existUser.getEmail().equals(user.getEmail())){
                log.trace("Пользователь с Login: {} и Email: {} уже существует", user.getLogin(), user.getEmail());
                throw new DuplicatedDataException("Пользователь с таким Login и Email уже зарегистрирован");
            } else if (existUser.getLogin().equals(user.getLogin())){
                log.trace("Пользователь с Login: {}", user.getLogin());
                throw new DuplicatedDataException("Пользователь с таким Login уже зарегистрирован");
            }
            log.trace("Пользователь с Email: {} уже существует", user.getEmail());
            throw new DuplicatedDataException("Пользователь с таким Email уже зарегистрирован");
        }

        return userRepository.create(user);
    }

    public User update(User user) {
        Optional<User> alreadyExistUser = userRepository.findDuplicateDataUser(user.getEmail(), user.getLogin());

        if(alreadyExistUser.isPresent()){
            if(alreadyExistUser.get().getId() != user.getId()){
                log.trace("Login: {} или Email: {} уже используются", user.getLogin(), user.getEmail());
                throw new DuplicatedDataException("Такой Login или Email уже используется");
            }
        }

        return userRepository.update(user);
    }

    public User getUserById(int id) {
        return userRepository.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

}
