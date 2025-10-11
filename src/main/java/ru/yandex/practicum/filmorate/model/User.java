package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class User {

    @Null(groups = Marker.OnCreate.class)
    @NotNull(groups = Marker.OnUpdate.class)
    @Positive(groups = Marker.OnUpdate.class)
    private Integer id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    @NotNull
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не должен содержать пробелы")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    @NotNull
    private LocalDate birthday;

}
