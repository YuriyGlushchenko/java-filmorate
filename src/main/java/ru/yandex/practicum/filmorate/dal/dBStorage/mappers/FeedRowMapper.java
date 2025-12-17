package ru.yandex.practicum.filmorate.dal.dBStorage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.FeedOperation;
import ru.yandex.practicum.filmorate.model.FeedType;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedRowMapper implements RowMapper<Feed> {

    @Override
    public Feed mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Feed.builder()
                .eventId(rs.getInt("event_id"))
                .timestamp(rs.getLong("create_time"))
                .feedType(FeedType.valueOf(rs.getString("type")))
                .feedOperation(FeedOperation.valueOf(rs.getString("operation")))
                .userId(rs.getInt("user_id"))
                .entityId((rs.getInt("entity_id")))
                .build();
    }
}