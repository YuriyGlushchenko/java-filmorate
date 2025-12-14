package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {

    Collection<Review> findAll();

    Optional<Review> getReviewById(int id);

    Review create(Review review);

    Review update(Review review);

    void delete(int id);

    boolean isExists(int id);

    //void increaseScore(Review review);

    //void decreaseScore(Review review);

    void updateUseful(int reviewId);

    void addLikeToReview(Integer reviewId, Integer userId);

    void deleteLikeFromReview(Integer reviewId, Integer userId);

    void addDislikeToReview(Integer reviewId, Integer userId);

    void deleteDislikeFromReview(Integer reviewId, Integer userId);
}