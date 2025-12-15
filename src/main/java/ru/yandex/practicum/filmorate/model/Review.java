package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import ru.yandex.practicum.filmorate.validators.Marker;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Review {

    @NotNull(groups = Marker.OnUpdate.class, message = "При обновлении отзыва id не может быть null")
    @Positive(groups = Marker.OnUpdate.class, message = "При обновлении отзыва id должен быть положительным целым числом")
    private Integer reviewId;

    @NotBlank
    private String content;

    @NotNull
    private Boolean isPositive;

    @NotNull
    private Integer userId;

    @NotNull
    private Integer filmId;

    @PositiveOrZero
    private int useful;
}

