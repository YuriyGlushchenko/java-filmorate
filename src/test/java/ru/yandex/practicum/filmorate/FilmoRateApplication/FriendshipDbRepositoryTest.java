package ru.yandex.practicum.filmorate.FilmoRateApplication;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.FriendshipStorage;
import ru.yandex.practicum.filmorate.dal.dBStorage.FriendshipDbRepository;
import ru.yandex.practicum.filmorate.dal.dBStorage.mappers.FriendshipRowMapper;
import ru.yandex.practicum.filmorate.dal.dBStorage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FriendshipDbRepository.class, FriendshipRowMapper.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FriendshipDbRepositoryTest {
    private final FriendshipStorage friendshipRepository;

    private Friendship testFriendship;

    @BeforeEach
    void setUp() {
        // Используем пользователей, которые еще не дружат в data.sql: 3 и 6
        testFriendship = Friendship.builder()
                .userId(3)
                .friendId(6)
                .status(FriendshipStatus.CONFIRMED)
                .build();
    }

    @Test
    public void testCreateFriendship() {
        Friendship createdFriendship = friendshipRepository.create(testFriendship);

        assertThat(createdFriendship).isNotNull();
        assertThat(createdFriendship.getId() > 0).isTrue();
        assertThat(createdFriendship.getUserId()).isEqualTo(3);
        assertThat(createdFriendship.getFriendId()).isEqualTo(6);
        assertThat(createdFriendship.getStatus()).isEqualTo(FriendshipStatus.CONFIRMED);
    }

    @Test
    public void testGetUserFriends() {
        // Из data.sql: пользователь 1 имеет друзей 2, 3 (подтвержденные, дружба односторонняя) и 4 (не подтвержденная)
        List<User> friends = friendshipRepository.getUserFriends(1);

        assertThat(friends).isNotNull();
        // Проверяем, что найдены 3 друга
        assertThat(friends.size() == 3).isTrue();

        // Проверяем, что возвращаются корректные пользователи с данными
        if (!friends.isEmpty()) {
            User firstFriend = friends.get(0);
            assertThat(firstFriend.getEmail()).isNotNull();
            assertThat(firstFriend.getLogin()).isNotNull();
            assertThat(firstFriend.getName()).isNotNull();
        }
    }

    @Test
    public void testRemoveFromFriends() {
        // Сначала создаем дружбу между 3 и 6
        Friendship createdFriendship = friendshipRepository.create(testFriendship);
        int userId = createdFriendship.getUserId();
        int friendId = createdFriendship.getFriendId();

        // Убеждаемся, что дружба существует
        List<User> friendsBefore = friendshipRepository.getUserFriends(userId);
        boolean friendExistsBefore = friendsBefore.stream()
                .anyMatch(user -> user.getId() == friendId);

        // Удаляем дружбу
        friendshipRepository.removeFromFriends(userId, friendId);

        // Проверяем, что дружба удалена
        List<User> friendsAfter = friendshipRepository.getUserFriends(userId);
        boolean friendExistsAfter = friendsAfter.stream()
                .anyMatch(user -> user.getId() == friendId);
        assertThat(friendExistsAfter).isFalse();
    }

    @Test
    public void testFindCommonFriends() {
        // Из data.sql:
        // пользователь 1 имеет друзей 2, 3 (подтвержденные, дружба односторонняя) и 4 (не подтвержденная)
        // пользователь 2 имеет друзей 3, 5 (подтвержденные, дружба односторонняя)
        // Общие друзья: 3

        List<User> commonFriends = friendshipRepository.findCommonFriends(1, 2);

        assertThat(commonFriends).isNotNull();
        assertThat(commonFriends.size() == 1).isTrue();

        // Проверяем наличие общего друга
        boolean hasCommonFriends = commonFriends.stream()
                .anyMatch(user -> user.getId() == 3);

        assertThat(hasCommonFriends).isTrue();

        // Проверяем, что общие друзья не включают самих пользователей
        boolean hasUser1 = commonFriends.stream().anyMatch(user -> user.getId() == 1);
        boolean hasUser2 = commonFriends.stream().anyMatch(user -> user.getId() == 2);
        assertThat(hasUser1).isFalse();
        assertThat(hasUser2).isFalse();
    }


    @Test
    public void testFindCommonFriendsWithSameUser() {
        // Поиск общих друзей одного и того же пользователя
        List<User> commonFriends = friendshipRepository.findCommonFriends(1, 1);

        assertThat(commonFriends).isNotNull();
        // Должны вернуться все друзья пользователя 1
        assertThat(commonFriends.size() > 0).isTrue();
    }

    @Test
    public void testFriendshipWithPENDINGStatus() {
        // Тестируем создание дружбы с разными статусами
        // Используем пользователей, которые еще не дружат: 4 и 6
        Friendship pendingFriendship = Friendship.builder()
                .userId(4)
                .friendId(6)
                .status(FriendshipStatus.PENDING)
                .build();

        Friendship createdPending = friendshipRepository.create(pendingFriendship);
        assertThat(createdPending).isNotNull();
        assertThat(createdPending.getStatus()).isEqualTo(FriendshipStatus.PENDING);
    }

    @Test
    public void testGetUserFriendsIncludesAllStatuses() {
        // Метод getUserFriends() должен возвращать друзей независимо от статуса
        // Создаем дружбу со статусом PENDING между существующими пользователями 2 и 6
        Friendship pendingFriendship = Friendship.builder()
                .userId(2)
                .friendId(6)
                .status(FriendshipStatus.PENDING)
                .build();
        friendshipRepository.create(pendingFriendship);

        List<User> friends = friendshipRepository.getUserFriends(2);

        // Проверяем, что новый друг добавлен в список (независимо от статуса)
        boolean hasNewFriend = friends.stream().anyMatch(user -> user.getId() == 6);
        assertThat(hasNewFriend).isTrue();
    }

    @Test
    public void testRemoveExistingFriendship() {
        // Удаляем существующую дружбу из data.sql между 1 и 2
        friendshipRepository.removeFromFriends(1, 2);

        List<User> friendsAfter = friendshipRepository.getUserFriends(1);
        boolean hasFriend2 = friendsAfter.stream().anyMatch(user -> user.getId() == 2);
        assertThat(hasFriend2).isFalse();
    }

    @Test
    public void testEmptyResults() {
        // Тестируем методы с пользователями, у которых нет друзей
        List<User> noFriends = friendshipRepository.getUserFriends(999);
        assertThat(noFriends).isNotNull();
        assertThat(noFriends.isEmpty()).isTrue();

        List<User> noCommonFriends = friendshipRepository.findCommonFriends(999, 888);
        assertThat(noCommonFriends).isNotNull();
        assertThat(noCommonFriends.isEmpty()).isTrue();
    }

    // ***************************

    @Test
    public void testFindCommonFriendsNoCommon() {
        // Используем пользователей, у которых точно нет общих друзей (из data.sql)
        // Пользователь 4 имеет только друга 5
        // Пользователь 6 имеет только друга 1
        List<User> commonFriends = friendshipRepository.findCommonFriends(4, 6);
        assertThat(commonFriends).isNotNull();

        // Ожидаем, что общих друзей нет -> список друзей пуст
        assertThat(commonFriends.isEmpty()).isTrue();
    }

    @Test
    public void testFriendshipDataIntegrity() {
        // Проверяем, что созданная дружба корректно сохраняет все поля
        // Используем пользователей, которые еще не дружат: 2 и 6
        Friendship friendship = Friendship.builder()
                .userId(2)
                .friendId(6)
                .status(FriendshipStatus.PENDING)
                .build();

        Friendship created = friendshipRepository.create(friendship);

        assertThat(created.getId() > 0).isTrue();
        assertThat(created.getUserId()).isEqualTo(2);
        assertThat(created.getFriendId()).isEqualTo(6);
        assertThat(created.getStatus()).isEqualTo(FriendshipStatus.PENDING);
        assertThat(created.getStatus().getStatusId() == 1).isTrue(); // PENDING status_id = 1
    }
}