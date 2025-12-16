package ru.yandex.practicum.filmorate.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserDTO {
    private int id;
    private String email;
    private String name;
    private LocalDate birthday;
    private String login;
}
