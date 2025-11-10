package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FriendDTO;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final FriendshipService friendshipService;

    @GetMapping
    public Collection<User> findAll() {
        return userService.getAllUsers();
    }


    @PostMapping
    @Validated({Marker.OnCreate.class})
    public User create(@Valid @RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping
    @Validated({Marker.OnUpdate.class})
    public User update(@RequestBody @Valid User newUser) {
        return userService.update(newUser);
    }

    @GetMapping("/{id}")
    public User findUserById(@PathVariable int id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addToFriends(@PathVariable("id") int userId, @PathVariable int friendId) {
        friendshipService.addToFriends(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFromFriends(@PathVariable("id") int userId, @PathVariable int friendId) {
        friendshipService.removeFromFriends(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<FriendDTO> getUserFriends(@PathVariable("id") int userId) {
        return friendshipService.getUserFriends(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getMutualFriends(@PathVariable int id, @PathVariable int otherId) {
        return friendshipService.getMutualFriends(id, otherId);
    }

}
