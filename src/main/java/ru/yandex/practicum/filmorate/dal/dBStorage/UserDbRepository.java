package ru.yandex.practicum.filmorate.dal.dBStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Repository("UserDbRepository")
@Slf4j
public class UserDbRepository extends BaseRepository<User> implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";

    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";

    private static final String FIND_BY_LOGIN_OR_EMAIL_QUERY = "SELECT * FROM users WHERE login = ? OR email = ?";

    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";

    private static final String REMOVE_BY_ID_QUERY = "DELETE FROM users WHERE user_id = ?;";

    public UserDbRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<User> getAllUsers() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<User> getUserById(int id) {

        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Optional<User> findDuplicateDataUser(String email, String login) {

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

    public void delete(int id) {
        delete(REMOVE_BY_ID_QUERY, id);
    }

    public List<Integer> checkUserIds(int[] userIds) {
        // получаем строку вида "?", "?", "?" с количеством плейсхолдеров "?" равным длине массива userIds
        String placeholders = String.join(",", Collections.nCopies(userIds.length, "?"));

        // вставляем нужное количество "?" в запрос
        String query = "SELECT user_id FROM users WHERE user_id IN (" + placeholders + ")";

        // выполняем запрос, подставляя вместо "?" значения из массива userIds (который предварительно запаковали в Integer)
        return jdbc.queryForList(query, Integer.class, Arrays.stream(userIds).boxed().toArray());
    }


}
