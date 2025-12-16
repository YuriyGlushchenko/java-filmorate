package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UserDTO;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<UserDTO> findAll() {
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

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        userService.delete(id);
    }

    @GetMapping("/{id}")
    public User findUserById(@PathVariable int id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addToFriends(@PathVariable("id") int userId, @PathVariable int friendId) {
        userService.addToFriends(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFromFriends(@PathVariable("id") int userId, @PathVariable int friendId) {
        userService.removeFromFriends(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<UserDTO> getUserFriends(@PathVariable("id") int userId) {
        return userService.getUserFriends(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> findCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        return userService.findCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/recommendations")
    public Collection<Film> getRecommendations(@PathVariable("id") int userId) {
        Collection<Film> recomendations = userService.getRecommendations(userId);

        if(recomendations.isEmpty()){
            return null;
        }
        return userService.getRecommendations(userId);


    }

}
