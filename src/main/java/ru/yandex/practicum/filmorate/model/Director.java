package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validators.Marker;

@Data
@Builder
public class Director {


    @NotNull(groups = Marker.OnUpdate.class, message = "При обновлении режиссёра id не может быть null")
    private Integer id;

    @Size(max = 255, message = "Имя режиссёра не должно превышать 255 символов")
//    @Pattern(regexp = "^[a-zA-Zа-яА-ЯёЁ\\s\\-]+$", message = "Имя режиссёра должно содержать только буквы, пробелы и дефисы")
    @NotBlank
    private String name;

}