package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.util.Collection;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        return reviewService.create(review);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable("id") int id) {
        return reviewService.getReviewById(id);
    }

    @PutMapping
    @Validated({Marker.OnUpdate.class})
    public Review update(@Valid @RequestBody Review review) {
        return reviewService.update(review);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        reviewService.delete(id);
    }

    @GetMapping
    public Collection<Review> findAll(
            @RequestParam(name = "filmId", defaultValue = "0", required = false) int filmId,
            @RequestParam(name = "count", defaultValue = "10", required = false) int count
    ) {
        return reviewService.findAll(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeReview(@PathVariable int id, @PathVariable int userId) {
        reviewService.addLikeToReview(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislikeReview(@PathVariable int id, @PathVariable int userId) {
        reviewService.addDislikeToReview(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLikeOfReview(@PathVariable int id, @PathVariable int userId) {
        reviewService.deleteLikeFromReview(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislikeOfReview(@PathVariable int id, @PathVariable int userId) {
        reviewService.deleteDislikeFromReview(id, userId);
    }
}

