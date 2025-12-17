package ru.yandex.practicum.filmorate.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.dal.dBStorage.FeedDBRepository;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;

import java.util.Collection;

@Validated
@RequiredArgsConstructor
@Service
public class FeedService {
    private final FeedDBRepository feedRepository;
    private final UserStorage userRepository;

    public Collection<Feed> findFeeds(@NotNull Integer userId) {
        userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Операция не выполнена. Пользователь с id=" + userId + " не найден"));
        return feedRepository.getAllFeedById(userId);
    }

    public Feed create(Feed feed) {
        return feedRepository.create(feed);
    }
}
