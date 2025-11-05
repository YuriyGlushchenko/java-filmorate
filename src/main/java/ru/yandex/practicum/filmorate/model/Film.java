package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validators.After;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Data
@Builder(toBuilder = true)
public class Film {

    @Null(groups = Marker.OnCreate.class, message = "При создании фильма id должен быть null")
    @NotNull(groups = Marker.OnUpdate.class, message = "При обновлении фильма id не может быть null")
    @Positive(groups = Marker.OnUpdate.class, message = "При обновлении фильма id должен быть положительным целым числом")
    private Integer id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    @After(value = "1895-12-28", message = "Дата релиза должна быть после 28 декабря 1895 года")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительной")
    private int duration;

    @JsonIgnore  // Будет нельзя накручивать лайки просто указав их в передаваемом json
    private final Set<Integer> likesUserIds = new HashSet<>();

    private MpaRating mpa;

    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

}
