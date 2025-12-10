package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validators.Marker;

@Data
@Builder
public class Director {

    @Null(groups = Marker.OnCreate.class, message = "При создании режиссёра id должен быть null")
    @NotNull(groups = Marker.OnUpdate.class, message = "При обновлении режиссёра id не может быть null")
    private Integer id;

    @NotBlank
    private String name;

}