package ru.yandex.practicum.filmorate.dal.dBStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.ReviewStorage;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Component
@Repository("ReviewDBRepository")
public class ReviewDBRepository extends BaseRepository<Review> implements ReviewStorage {
    private static final String FIND_ALL_REVIEWS_QUERY = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";

    private static final String FIND_REVIEW_BY_ID_QUERY = "SELECT * FROM reviews WHERE review_id = ?";

    private static final String FIND_REVIEW_BY_FILM_ID = "SELECT * FROM reviews WHERE film_id = ? " +
            "ORDER BY useful DESC LIMIT ?";

    private static final String INSERT_QUERY = "INSERT INTO reviews(content, is_positive, user_id, film_id) " +
            "VALUES (?, ?, ?, ?)";

    private static final String UPDATE_QUERY = "UPDATE reviews SET content = ?, is_positive = ?WHERE review_id = ?";

    private static final String DELETE_REVIEW_QUERY = "DELETE FROM reviews WHERE review_id = ?";

    private static final String UPDATE_USEFUL_REVIEW =
            "UPDATE reviews SET useful = (SELECT SUM(CASE WHEN is_positive = TRUE THEN 1 ELSE -1 END) useful " +
                    "FROM review_likes WHERE review_id =?) WHERE review_id = ?";

    public ReviewDBRepository(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Review> findAll(int count) {
        return findMany(FIND_ALL_REVIEWS_QUERY, count);
    }

    @Override
    public Optional<Review> getReviewById(int id) {
        return findOne(FIND_REVIEW_BY_ID_QUERY, id);
    }

    @Override
    public Collection<Review> getAllReviewById(int filmId, int count) {
        return findMany(FIND_REVIEW_BY_FILM_ID, filmId, count);
    }

    @Override
    public Review create(Review review) {
        int id = insert(
                INSERT_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId()
        );

        review.setReviewId(id);
        return review;
    }

    @Override
    public Review update(Review review) {
        update(UPDATE_QUERY, review.getContent(), review.getIsPositive(), review.getReviewId());
        return review;
    }

    @Override
    public void delete(int id) {
        delete(DELETE_REVIEW_QUERY, id);
    }

    @Override
    public boolean isNotExists(int id) {
        String checkQuery = "SELECT EXISTS(SELECT 1 FROM reviews WHERE review_id = ?)";

        Boolean exists = jdbc.queryForObject(checkQuery, Boolean.class, id);

        return exists == null || !exists;
    }

    public void updateUseful(int reviewId) {
        update(UPDATE_USEFUL_REVIEW, reviewId, reviewId);
    }

    @Override
    public void addReaction(Integer reviewId, Integer userId, Boolean isPositive) {
        String mergeReactionQuery = "MERGE INTO review_likes (review_id, user_id, is_positive) VALUES (?, ?, ?)";

        update(mergeReactionQuery, reviewId, userId, isPositive);
        updateUseful(reviewId);
    }

    @Override
    public void removeReaction(Integer reviewId, Integer userId) {
        String deleteReactionQuery = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";

        delete(deleteReactionQuery, reviewId, userId);
        updateUseful(reviewId);
    }
}
