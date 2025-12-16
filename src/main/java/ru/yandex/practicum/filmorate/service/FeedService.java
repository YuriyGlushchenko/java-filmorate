package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.dal.dBStorage.FeedDBRepository;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;

@Slf4j
@Validated
@RequiredArgsConstructor
@Service
public class FeedService {
    private final FeedDBRepository feedRepository;

    public Collection<Feed> findFeeds(Integer userId) {
        return feedRepository.getAllFeedById(userId);
    }

    public Feed create(Feed feed) {
        return feedRepository.create(feed);
    }
}
