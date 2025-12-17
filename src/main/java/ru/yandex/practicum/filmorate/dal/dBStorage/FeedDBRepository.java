package ru.yandex.practicum.filmorate.dal.dBStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.FeedStorage;
import ru.yandex.practicum.filmorate.model.Feed;

import java.util.Collection;

@Repository
@Slf4j
public class FeedDBRepository extends BaseRepository<Feed> implements FeedStorage {

    private static final String FIND_FEED_BY_USER_ID = "SELECT * FROM feeds WHERE user_id = ? " +
            "ORDER BY create_time";

    private static final String INSERT_QUERY = "INSERT INTO feeds(create_time, type, operation, user_id, entity_id) " +
            "VALUES (?, ?, ?, ?, ?)";

    public FeedDBRepository(JdbcTemplate jdbc, RowMapper<Feed> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Feed> getAllFeedById(int userId) {
        return findMany(FIND_FEED_BY_USER_ID, userId);
    }

    @Override
    public Feed create(Feed feed) {
        int id = insert(
                INSERT_QUERY,
                feed.getTimestamp(),
                feed.getFeedType().name(),
                feed.getFeedOperation().name(),
                feed.getUserId(),
                feed.getEntityId()
        );

        feed.setEventId(id);
        return feed;
    }
}