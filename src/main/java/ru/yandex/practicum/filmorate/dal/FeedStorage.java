package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Feed;

import java.util.Collection;

public interface FeedStorage {

    Collection<Feed> getAllFeedById(int userId);

    Feed create(Feed feed);
}
