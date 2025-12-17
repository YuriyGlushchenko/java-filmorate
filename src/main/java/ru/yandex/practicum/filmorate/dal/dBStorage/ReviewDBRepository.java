package ru.yandex.practicum.filmorate.dal.dBStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
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

    private static final String MERGE_LIKE_REVIEW_QUERY =
            "MERGE INTO review_likes (review_id, user_id, is_positive) VALUES (?, ?, ?)";

    private static final String DELETE_REVIEW_LIKE_QUERY = "DELETE FROM review_likes WHERE review_id=? AND user_id=?";

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
        log.debug("isNotExists({})", id);
        try {
            getReviewById(id);
            log.trace("Информация по отзыву с идентификатором {} найдена", id);
            return false;
        } catch (EmptyResultDataAccessException exception) {
            log.trace("Нет информации по отзыву с идентификатором {}", id);
            return true;
        }
    }

    public void updateUseful(int reviewId) {
        update(UPDATE_USEFUL_REVIEW, reviewId, reviewId);
    }

    @Override
    public void addLikeToReview(Integer reviewId, Integer userId) {
        update(MERGE_LIKE_REVIEW_QUERY, reviewId, userId, Boolean.TRUE);
        updateUseful(reviewId);
    }

    @Override
    public void deleteLikeFromReview(Integer reviewId, Integer userId) {
        delete(DELETE_REVIEW_LIKE_QUERY, reviewId, userId);
        updateUseful(reviewId);
    }

    @Override
    public void addDislikeToReview(Integer reviewId, Integer userId) {
        update(MERGE_LIKE_REVIEW_QUERY, reviewId, userId, Boolean.FALSE);
        updateUseful(reviewId);
    }

    @Override
    public void deleteDislikeFromReview(Integer reviewId, Integer userId) {
        delete(DELETE_REVIEW_LIKE_QUERY, reviewId, userId);
        updateUseful(reviewId);
    }
}
