package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;

@Data
@Builder
public class User {
    private int id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    @NonNull
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не должен содержать пробелы")
    @NonNull
    private String login;

    private String name;

    @Past(message = "Дата рождения должна быть в прошлом")
    @NonNull
    private LocalDate birthday;

//    public User(String email, String login, String name, LocalDate birthday) {
//        this.email = email;
//        this.login = login;
//        this.name = name;
//        this.birthday = birthday;
//    }
}
