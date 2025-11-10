package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FriendshipStorage;
import ru.yandex.practicum.filmorate.exceptions.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dal.UserStorage;

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
    public UserService(
            @Value("#{@${filmorate-app.storage.user-repository}}") UserStorage userRepository,
            FriendshipStorage friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    public Collection<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

//    public List<UserDto> getUsers() {
//        return userRepository.findAll()
//                .stream()
//                .map(UserMapper::mapToUserDto)
//                .collect(Collectors.toList());
//    }

    public User create(User user) {
        Optional<User> alreadyExistUser = userRepository.findDuplicateUser(user.getEmail(), user.getLogin());

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
        Optional<User> alreadyExistUser = userRepository.findDuplicateUser(user.getEmail(), user.getLogin());

        if(alreadyExistUser.isPresent()){
            if(alreadyExistUser.get().getId() != user.getId()){
                log.trace("Login: {} или Email: {} уже используются", user.getLogin(), user.getEmail());
                throw new DuplicatedDataException("Такой Login или Email уже используется");
            }
        }

        return userRepository.update(user);
    }

    public User getUserById(int id) {
        Optional<User> user = userRepository.getUserById(id);



        return userRepository.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
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
        User user = userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + "не найден"));

        return user
                .getFriends()
                .stream()
                .map(id -> userRepository.getUserById(id).get())
                .collect(Collectors.toList());
    }

    public List<User> getMutualFriends(int userA, int userB) {
        List<User> userPair = correctUserPair(userA, userB);

        return userPair
                .getFirst()
                .getFriends()
                .stream()
                .filter(id -> userPair.getLast().getFriends().contains(id))
                .map(id -> userRepository
                        .getUserById(id)
                        .orElseThrow(() -> new NotFoundException("В списке друзей несуществующий пользователь с id = " + id)))
                .collect(Collectors.toList());
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
