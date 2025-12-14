package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.filmorate.validators.Marker;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder//(toBuilder = true)
public class Review {

    //@Null(groups = Marker.OnCreate.class, message = "При создании отзыва id должен быть null")
    @NotNull(groups = Marker.OnUpdate.class, message = "При обновлении отзыва id не может быть null")
    //@Positive(groups = Marker.OnUpdate.class, message = "При обновлении отзыва id должен быть положительным целым числом")
    private Integer id;

    @NotBlank
    private String content;

    @NotNull
    private Boolean isPositive;

    @NotNull
    private Integer userId;

    @NotNull
    private Integer filmId;

    @PositiveOrZero
    private Integer useful;
}

