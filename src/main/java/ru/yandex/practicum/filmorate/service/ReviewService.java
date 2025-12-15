package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.dal.ReviewStorage;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewRepository;
    private final UserService userService;
    private final FilmService filmService;

    public Review create(Review review) {
        checker(review.getFilmId(), review.getUserId());
        return reviewRepository.create(review);
    }

    public Review update(Review review) {
        checker(review.getFilmId(), review.getUserId());
        reviewRepository.getReviewById(review.getReviewId())
                .orElseThrow(() -> new NotFoundException("Данные не обновлены. Отзыв с id=" + review.getReviewId() + " не найден"));
        return reviewRepository.update(review);
    }

    public void delete(Integer id) {
        if (reviewRepository.isNotExists(id))
            throw new NotFoundException("Отзыв не найден: пустой или неправильный идентификатор");
        reviewRepository.delete(id);
    }

    public Review getReviewById(Integer id) {
        return reviewRepository.getReviewById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + id + " не найден"));
    }

    public Collection<Review> findAll(Integer filmId, int count) {
        if (filmId == null)
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
        if (filmId == null || filmService.getFilmById(filmId) == null) {
            throw new NotFoundException("Не найден фильм c идентификатором " + filmId);
        }
        if (userId == null || userService.getUserById(userId) == null) {
            throw new NotFoundException("Не найден пользователь с идентификатором " + userId);
        }
    }
}

