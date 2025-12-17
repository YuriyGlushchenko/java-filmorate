package ru.yandex.practicum.filmorate.service;

import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.dal.dBStorage.FeedDBRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class FeedService {
    private final FeedDBRepository feedRepository;

    public Collection<Feed> findFeeds(@NotNull Integer userId) {
        return feedRepository.getAllFeedById(userId);
    }

    public Feed create(Feed feed) {
        return feedRepository.create(feed);
    }
}
