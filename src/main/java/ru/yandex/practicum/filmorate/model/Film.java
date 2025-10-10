package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.Duration;
import java.time.LocalDate;


@Data
@Builder
public class Film {
    private int id;

    @NonNull
    @NotBlank
    private String name;

    @Size(max=200)
    private String description;


    private LocalDate releaseDate;

    @Positive
    private int duration;
}
