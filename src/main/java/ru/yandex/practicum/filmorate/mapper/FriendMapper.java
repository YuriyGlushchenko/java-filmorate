package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FriendDTO;
import ru.yandex.practicum.filmorate.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FriendMapper {
//    public static User mapToUser(NewUserRequest request) {
//        User user = new User();
//        user.setUsername(request.getUsername());
//        user.setPassword(request.getPassword());
//        user.setEmail(request.getEmail());
//        user.setRegistrationDate(Instant.now());
//
//        return user;
//    }

    public static FriendDTO mapToFriendDto(User user) {
        return FriendDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();
//        dto.setId(user.getId());
//        dto.setUsername(user.getUsername());
//        dto.setEmail(user.getEmail());
//        dto.setRegistrationDate(Instant.now());
//        return dto;
    }

//    public static User updateUserFields(User user, UpdateUserRequest request) {
//        if (request.hasEmail()) {
//            user.setEmail(request.getEmail());
//        }
//        if (request.hasPassword()) {
//            user.setPassword(request.getPassword());
//        }
//        if (request.hasUsername()) {
//            user.setUsername(request.getUsername());
//        }
//        return user;
//    }
}