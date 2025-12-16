package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.dal.FeedStorage;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.dal.ReviewStorage;
import static ru.yandex.practicum.filmorate.model.FeedType.*;
import static ru.yandex.practicum.filmorate.model.FeedOperation.*;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.Collection;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewRepository;
    private final FilmStorage filmRepository;
    private final UserStorage userRepository;
    private final FeedStorage feedRepository;

    public Review create(Review review) {
        checker(review.getFilmId(), review.getUserId());

        Review createdReview = reviewRepository.create(review);

        Feed createdFeed = Feed.builder()
                .timestamp(Instant.now().toEpochMilli())
                .feedType(REVIEW)
                .feedOperation(ADD)
                .userId(review.getUserId())
                .entityId(review.getReviewId())
                .build();
        feedRepository.create(createdFeed);

        return createdReview;
    }

    public Review update(Review review) {
        Review uploadedReview = reviewRepository.getReviewById(review.getReviewId())
                .orElseThrow(() -> new NotFoundException("Данные не обновлены. Отзыв с id=" + review.getReviewId() + " не найден"));
        review.setFilmId(uploadedReview.getFilmId());
        review.setUserId(uploadedReview.getUserId());

        Review updatedReview = reviewRepository.update(review);
        Feed createdFeed = Feed.builder()
                .timestamp(Instant.now().toEpochMilli())
                .feedType(REVIEW)
                .feedOperation(UPDATE)
                .userId(review.getUserId())
                .entityId(review.getReviewId())
                .build();
        feedRepository.create(createdFeed);


        return updatedReview;
    }

    public void delete(Integer id) {
        Review review = reviewRepository.getReviewById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден: пустой или неправильный идентификатор"));

        reviewRepository.delete(id);

        Feed createdFeed = Feed.builder()
                .timestamp(Instant.now().toEpochMilli())
                .feedType(REVIEW)
                .feedOperation(REMOVE)
                .userId(review.getUserId())
                .entityId(review.getReviewId())
                .build();
        feedRepository.create(createdFeed);
    }

    public Review getReviewById(Integer id) {
        return reviewRepository.getReviewById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + id + " не найден"));
    }

    public Collection<Review> findAll(Integer filmId, int count) {
        if (filmId == 0)
            return reviewRepository.findAll(count);
        return reviewRepository.getAllReviewById(filmId, count);
    }

    public void addLikeToReview(Integer reviewId, Integer userId) {
        reviewRepository.addLikeToReview(reviewId, userId);
    }

    public void addDislikeToReview(Integer reviewId, Integer userId) {
        reviewRepository.addDislikeToReview(reviewId, userId);
    }

    public void deleteLikeFromReview(Integer reviewId, Integer userId) {
        reviewRepository.deleteLikeFromReview(reviewId, userId);
    }

    public void deleteDislikeFromReview(Integer reviewId, Integer userId) {
        reviewRepository.deleteDislikeFromReview(reviewId, userId);
    }

    private void checker(Integer filmId, Integer userId) {
        if (filmId == null || filmRepository.getFilmById(filmId).isEmpty()) {
            throw new NotFoundException("Не найден фильм c идентификатором " + filmId);
        }
        if (userId == null || userRepository.getUserById(userId).isEmpty()) {
            throw new NotFoundException("Не найден пользователь с идентификатором " + userId);
        }
    }
}

