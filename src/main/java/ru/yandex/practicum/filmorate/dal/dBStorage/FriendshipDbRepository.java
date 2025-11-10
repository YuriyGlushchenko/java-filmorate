package ru.yandex.practicum.filmorate.dal.dBStorage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.FriendshipStorage;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

@Repository("FriendshipDbRepository")
public class FriendshipDbRepository extends BaseRepository<Friendship> implements FriendshipStorage {
    private static final String INSERT_QUERY = "INSERT INTO friendship (user_id, friend_id, status_id) VALUES (?, ?, ?)";
    private static final String FIND_BY_USERS_IDS_QUERY = "SELECT * FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_USER_FRIENDS = "SELECT f.friend_id As user_id, uf.email, uf.login, uf.name, uf.birthday" +
            " FROM friendship AS f JOIN users AS uf ON f.friend_id = uf.user_id WHERE f.user_id = ? ;";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String FIND_BY_LOGIN_QUERY = "SELECT * FROM users WHERE login = ?";
    private static final String FIND_BY_LOGIN_OR_EMAIL_QUERY = "SELECT * FROM users WHERE login = ? OR email = ?";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";

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
    public Optional<Friendship> getFriendshipByUsersIds(int userId, int friendId) {
        return findOne(FIND_BY_USERS_IDS_QUERY, userId, friendId);
    }

    @Override
    public List<User> getUserFriends(int userId) {
        // Без учета статуса дружбы, что странно. Если с учетом статуса, то тесты (которые приложены к ТЗ) не проходят.
        return jdbc.query(FIND_USER_FRIENDS, userMapper, userId);
    }

    @Override
    public void removeFromFriends(int userId, int friendId){
        delete(DELETE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public List<User> getMutualFriends(int userA, int userB) {
        return List.of();
    }
}
