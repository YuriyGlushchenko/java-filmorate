package ru.yandex.practicum.filmorate.dal.dBStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exceptions.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository("UserDbRepository")
@Slf4j
public class UserDbRepository extends BaseRepository<User> implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = ?";
    private static final String FIND_BY_LOGIN_QUERY = "SELECT * FROM users WHERE login = ?";
    private static final String FIND_BY_LOGIN_OR_EMAIL_QUERY = "SELECT * FROM users WHERE login = ? OR email = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";

    public UserDbRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<User> getAllUsers() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<User> getUserById(int id) {
        Optional<User> userOptional = findOne(FIND_BY_ID_QUERY, id);
        userOptional.ifPresent(this::loadUserFriends);

        return userOptional;
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return findOne(FIND_BY_EMAIL_QUERY, email);
    }

    @Override
    public Optional<User> getUserByLogin(String login) {
        return findOne(FIND_BY_LOGIN_QUERY, login);
    }

    @Override
    public Optional<User> findDuplicateUser(String email, String login) {
        return findOne(FIND_BY_LOGIN_OR_EMAIL_QUERY, login, email);
    }

    @Override
    public User create(User user) {


        int id = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                java.sql.Date.valueOf(user.getBirthday()));

        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {

        Optional<User> userOptional = findOne(FIND_BY_ID_QUERY, user.getId());

        if(userOptional.isEmpty()){
            log.debug("Пользователь с id={} не найден", user.getId());
            throw new NotFoundException("Данные не обновлены. Пользователь с id=" + user.getId() + " не найден");
        }

        update(
                UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    private void loadUserFriends(User user) {
        String sql = "SELECT friend_id FROM friendship WHERE user_id = ? AND status_id = ?";

        List<Integer> friendIds = jdbc.query(
                sql,
                (rs, rowNum) -> rs.getInt("friend_id"),
                user.getId(),
                FriendshipStatus.CONFIRMED.getStatusId()
        );

        user.getFriends().clear();
        user.getFriends().addAll(friendIds);
    }

}
