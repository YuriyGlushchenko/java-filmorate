package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {

    Collection<Review> findAll(int count);

    Optional<Review> getReviewById(int id);

    Collection<Review> getAllReviewById(int filmId, int count);

    Review create(Review review);

    Review update(Review review);

    void delete(int id);

    boolean isNotExists(int id);

    void updateUseful(int reviewId);

    void addLikeToReview(Integer reviewId, Integer userId);

    void deleteLikeFromReview(Integer reviewId, Integer userId);

    void addDislikeToReview(Integer reviewId, Integer userId);

    void deleteDislikeFromReview(Integer reviewId, Integer userId);
}