package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class User {

    private final Set<Integer> friends = new HashSet<>();
    @Null(groups = Marker.OnCreate.class, message = "При создании пользователя id должен быть null")
    @NotNull(groups = Marker.OnUpdate.class, message = "При обновлении пользователя id не может быть null")
    @Positive(groups = Marker.OnUpdate.class, message = "При обновлении пользователя id должен быть положительным целым числом")
    private Integer id;
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;
    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не должен содержать пробелы")
    private String login;
    private String name;
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    @NotNull
    private LocalDate birthday;
}
