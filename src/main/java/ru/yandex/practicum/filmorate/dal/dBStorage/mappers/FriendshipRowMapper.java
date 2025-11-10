package ru.yandex.practicum.filmorate.dal.dBStorage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FriendshipRowMapper implements RowMapper<Friendship> {

    @Override
    public Friendship mapRow(ResultSet resultSet, int rowNum) throws SQLException {

        return Friendship.builder()
                .id(resultSet.getInt("friendship_id"))
                .userId(resultSet.getInt("user_id"))
                .friendId(resultSet.getInt("friend_id"))
                .status(FriendshipStatus.fromId(resultSet.getInt("status_id")))
                .build();
    }
}