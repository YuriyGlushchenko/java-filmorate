package ru.yandex.practicum.filmorate.dal.dBStorage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.FriendshipStorage;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Repository("FriendshipDbRepository")
public class FriendshipDbRepository extends BaseRepository<Friendship> implements FriendshipStorage {
    private static final String INSERT_QUERY = "INSERT INTO friendship (user_id, friend_id, status_id) VALUES (?, ?, ?)";
    private static final String FIND_USER_FRIENDS = "SELECT f.friend_id As user_id, uf.email, uf.login, uf.name, uf.birthday" +
            " FROM friendship AS f JOIN users AS uf ON f.friend_id = uf.user_id WHERE f.user_id = ? ;";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String COMMON_FRIENDS_QUERY = "SELECT u.* FROM friendship f1 JOIN friendship f2 ON " +
            "f1.friend_id = f2.friend_id JOIN users u ON f1.friend_id = u.user_id WHERE f1.user_id = ? AND f2.user_id = ?";

    private final RowMapper<User> userMapper;

    public FriendshipDbRepository(JdbcTemplate jdbc, RowMapper<Friendship> mapper, RowMapper<User> userMapper) {
        super(jdbc, mapper);
        this.userMapper = userMapper;
    }

    @Override
    public Friendship create(Friendship friendship) {
        int id = insert(
                INSERT_QUERY,
                friendship.getUserId(),
                friendship.getFriendId(),
                friendship.getStatus().getStatusId());

        friendship.setId(id);
        return friendship;
    }

    @Override
    public List<User> getUserFriends(int userId) {
        // Без учета статуса дружбы, что странно. Если с учетом статуса, то тесты (которые приложены к ТЗ) не проходят.
        return jdbc.query(FIND_USER_FRIENDS, userMapper, userId);
    }

    @Override
    public void removeFromFriends(int userId, int friendId) {
        delete(DELETE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public List<User> findCommonFriends(int userA, int userB) {
        // Без учета статуса дружбы
        return jdbc.query(COMMON_FRIENDS_QUERY, userMapper, userA, userB);
    }
}
