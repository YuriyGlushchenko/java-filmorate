package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.dal.dBStorage.UserDbRepository;
import ru.yandex.practicum.filmorate.dal.dBStorage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbRepository.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbRepositoryTest {
    private final UserStorage userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("newuser@mail.ru")
                .login("new_user_login")
                .name("Новый Тестовый Пользователь")
                .birthday(LocalDate.of(1995, 5, 15))
                .build();
    }

    @Test
    public void testFindUserById() {
        Optional<User> userOptional = userRepository.getUserById(1);

        assertThat(userOptional.isPresent()).isTrue();
        User user = userOptional.get();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getEmail()).isEqualTo("ivan@mail.ru");
        assertThat(user.getLogin()).isEqualTo("ivan_petrov");
        assertThat(user.getName()).isEqualTo("Иван Петров");
        assertThat(user.getFriends()).isNotNull();
    }

    @Test
    public void testFindUserByNotValidId() {
        Optional<User> userOptional = userRepository.getUserById(999);
        assertThat(userOptional.isPresent()).isFalse();
    }

    @Test
    public void testFindDuplicateDataUser() {
        Optional<User> userOptional = userRepository.getUserById(1);
        assertThat(userOptional.isPresent()).isTrue();

        String existingUserEmail = userOptional.get().getEmail();
        String existingUserLogin = userOptional.get().getLogin();

        Optional<User> duplicateEmailUser = userRepository.findDuplicateDataUser(existingUserEmail, "");
        assertThat(duplicateEmailUser.isPresent()).isTrue();
        assertThat(duplicateEmailUser.get().getEmail()).isEqualTo(existingUserEmail);

        Optional<User> duplicateLoginUser = userRepository.findDuplicateDataUser("", existingUserLogin);
        assertThat(duplicateLoginUser.isPresent()).isTrue();
        assertThat(duplicateLoginUser.get().getLogin()).isEqualTo(existingUserLogin);

        Optional<User> noDuplicateUser = userRepository.findDuplicateDataUser("такой почты нет", "такого логина нет");
        assertThat(noDuplicateUser.isPresent()).isFalse();
    }

    @Test
    public void testGetAllUsers() {
        Collection<User> allUsers = userRepository.getAllUsers();

        assertThat(allUsers).isNotNull();
        assertThat(allUsers.size() >= 6).isTrue();

        List<String> emails = allUsers.stream()
                .map(User::getEmail)
                .collect(Collectors.toList());

        assertThat(emails.contains("ivan@mail.ru")).isTrue();
        assertThat(emails.contains("maria@gmail.com")).isTrue();
        assertThat(emails.contains("alex@yandex.ru")).isTrue();

        Optional<User> ivan = allUsers.stream()
                .filter(u -> u.getEmail().equals("ivan@mail.ru"))
                .findFirst();
        assertThat(ivan.isPresent()).isTrue();

        Optional<User> maria = allUsers.stream()
                .filter(u -> u.getEmail().equals("maria@gmail.com"))
                .findFirst();
        assertThat(maria.isPresent()).isTrue();
    }

    @Test
    public void testCreateUser() {
        User createdUser = userRepository.create(testUser);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId() > 0).isTrue();
        assertThat(createdUser.getEmail()).isEqualTo("newuser@mail.ru");
        assertThat(createdUser.getLogin()).isEqualTo("new_user_login");
        assertThat(createdUser.getName()).isEqualTo("Новый Тестовый Пользователь");
        assertThat(createdUser.getBirthday()).isEqualTo(LocalDate.of(1995, 5, 15));

        Optional<User> retrievedUser = userRepository.getUserById(createdUser.getId());
        assertThat(retrievedUser.isPresent()).isTrue();
        assertThat(retrievedUser.get().getEmail()).isEqualTo("newuser@mail.ru");
    }

    @Test
    public void testUpdateUser() {
        User createdUser = userRepository.create(testUser);
        int userId = createdUser.getId();

        User updatedUser = User.builder()
                .id(userId)
                .email("updated@mail.ru")
                .login("updated_login")
                .name("Обновленное Имя")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User result = userRepository.update(updatedUser);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo("updated@mail.ru");
        assertThat(result.getLogin()).isEqualTo("updated_login");
        assertThat(result.getName()).isEqualTo("Обновленное Имя");
        assertThat(result.getBirthday()).isEqualTo(LocalDate.of(2000, 1, 1));

        Optional<User> retrievedUser = userRepository.getUserById(userId);
        assertThat(retrievedUser.isPresent()).isTrue();
        assertThat(retrievedUser.get().getEmail()).isEqualTo("updated@mail.ru");
        assertThat(retrievedUser.get().getLogin()).isEqualTo("updated_login");
    }

    @Test
    public void testUpdateNonExistentUser() {
        User nonExistentUser = User.builder()
                .id(999)
                .email("nonexistent@mail.ru")
                .login("nonexistent")
                .name("Несуществующий")
                .birthday(LocalDate.now())
                .build();

        assertThrows(NotFoundException.class, () -> {
            userRepository.update(nonExistentUser);
        });
    }

    @Test
    public void testUserFriendsLoading() {
        Optional<User> userOptional = userRepository.getUserById(1);

        assertThat(userOptional.isPresent()).isTrue();
        User user = userOptional.get();

        assertThat(user.getFriends()).isNotNull();

        // Текущая логика: ищет только WHERE user_id = 1 AND status_id = 2
        // Из data.sql: (1, 2, 2), (1, 3, 2) - найдет
        // (5, 1, 2) - НЕ найдет, потому что user_id = 5, а не 1
        // (1, 4, 1) - НЕ найдет, потому что status_id = 1

        assertThat(user.getFriends().size() == 2).isTrue(); // Только 2 и 3

        assertThat(user.getFriends().contains(2)).isTrue();  // (1,2,2)
        assertThat(user.getFriends().contains(3)).isTrue();  // (1,3,2)
        assertThat(user.getFriends().contains(5)).isFalse(); // (5,1,2) - не найдет!
        assertThat(user.getFriends().contains(4)).isFalse(); // (1,4,1) - не найдет
    }

    @Test
    public void testMultipleUserFriendsLoading() {
        // Пользователь 2: WHERE user_id = 2 AND status_id = 2
        // Из data.sql: (2, 3, 2), (2, 5, 2)
        Optional<User> user2 = userRepository.getUserById(2);
        assertThat(user2.isPresent()).isTrue();
        System.out.println("User 2 friends: " + user2.get().getFriends());

        assertThat(user2.get().getFriends().size() == 2).isTrue();
        assertThat(user2.get().getFriends().contains(1)).isFalse(); // (1,2,2) - не найдет, потому что user_id = 1
        assertThat(user2.get().getFriends().contains(3)).isTrue();  // (2,3,2)
        assertThat(user2.get().getFriends().contains(5)).isTrue();  // (2,5,2)

        // Пользователь 5: WHERE user_id = 5 AND status_id = 2
        // Из data.sql: (5, 1, 2), (5, 6, 2)
        Optional<User> user5 = userRepository.getUserById(5);
        assertThat(user5.isPresent()).isTrue();
        System.out.println("User 5 friends: " + user5.get().getFriends());

        assertThat(user5.get().getFriends().size() == 2).isTrue();
        assertThat(user5.get().getFriends().contains(1)).isTrue();  // (5,1,2)
        assertThat(user5.get().getFriends().contains(2)).isFalse(); // (2,5,2) - не найдет
        assertThat(user5.get().getFriends().contains(4)).isFalse(); // (4,5,2) - не найдет
        assertThat(user5.get().getFriends().contains(6)).isTrue();  // (5,6,2)
    }

    @Test
    public void testUserWithoutFriends() {
        User newUser = userRepository.create(testUser);

        Optional<User> retrievedUser = userRepository.getUserById(newUser.getId());
        assertThat(retrievedUser.isPresent()).isTrue();
        assertThat(retrievedUser.get().getFriends()).isNotNull();
        assertThat(retrievedUser.get().getFriends().isEmpty()).isTrue();
    }

    @Test
    public void testFindDuplicateDataUserWithBothParameters() {
        Optional<User> existingUser = userRepository.getUserById(1);
        assertThat(existingUser.isPresent()).isTrue();

        String existingEmail = existingUser.get().getEmail();
        String existingLogin = existingUser.get().getLogin();

        Optional<User> foundUser = userRepository.findDuplicateDataUser(existingEmail, existingLogin);
        assertThat(foundUser.isPresent()).isTrue();
    }

    @Test
    public void testUserDataConsistency() {
        Optional<User> user1 = userRepository.getUserById(1);
        assertThat(user1.isPresent()).isTrue();
        assertThat(user1.get().getEmail()).isEqualTo("ivan@mail.ru");
        assertThat(user1.get().getLogin()).isEqualTo("ivan_petrov");
        assertThat(user1.get().getName()).isEqualTo("Иван Петров");
        assertThat(user1.get().getBirthday()).isEqualTo(LocalDate.of(1990, 5, 15));

        Optional<User> user2 = userRepository.getUserById(2);
        assertThat(user2.isPresent()).isTrue();
        assertThat(user2.get().getEmail()).isEqualTo("maria@gmail.com");
        assertThat(user2.get().getLogin()).isEqualTo("maria_s");
        assertThat(user2.get().getName()).isEqualTo("Мария Сидорова");
        assertThat(user2.get().getBirthday()).isEqualTo(LocalDate.of(1985, 12, 3));
    }

    @Test
    public void testCreateAndRetrieveMultipleUsers() {
        User user1 = User.builder()
                .email("test1@mail.ru")
                .login("test1_login")
                .name("Тест 1")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User user2 = User.builder()
                .email("test2@mail.ru")
                .login("test2_login")
                .name("Тест 2")
                .birthday(LocalDate.of(2001, 2, 2))
                .build();

        User created1 = userRepository.create(user1);
        User created2 = userRepository.create(user2);

        assertThat(created1.getId() != created2.getId()).isTrue();

        Collection<User> allUsers = userRepository.getAllUsers();
        assertThat(allUsers.size() >= 8).isTrue();

        List<String> emails = allUsers.stream()
                .map(User::getEmail)
                .collect(Collectors.toList());

        assertThat(emails.contains("test1@mail.ru")).isTrue();
        assertThat(emails.contains("test2@mail.ru")).isTrue();
    }

    @Test
    public void testUpdateUserWithSameData() {
        User createdUser = userRepository.create(testUser);
        int userId = createdUser.getId();

        User sameDataUser = User.builder()
                .id(userId)
                .email("newuser@mail.ru")
                .login("new_user_login")
                .name("Новый Тестовый Пользователь")
                .birthday(LocalDate.of(1995, 5, 15))
                .build();

        User result = userRepository.update(sameDataUser);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo("newuser@mail.ru");

        Optional<User> retrievedUser = userRepository.getUserById(userId);
        assertThat(retrievedUser.isPresent()).isTrue();
        assertThat(retrievedUser.get().getEmail()).isEqualTo("newuser@mail.ru");
    }

    @Test
    public void testCreateUserWithNullFields() {
        User userWithNullName = User.builder()
                .email("noname@mail.ru")
                .login("noname_login")
                .name(null)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userRepository.create(userWithNullName);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId() > 0).isTrue();
        assertThat(createdUser.getName() == null).isTrue();
    }
}