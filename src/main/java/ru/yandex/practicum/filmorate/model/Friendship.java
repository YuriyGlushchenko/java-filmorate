package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Friendship {
    private Integer id;
    private Integer userId;          // ID пользователя, который отправляет запрос
    private Integer friendId;        // ID пользователя, который получает запрос
    private FriendshipStatus status;
}