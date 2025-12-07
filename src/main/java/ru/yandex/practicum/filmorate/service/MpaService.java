package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.MpaRatingStorage;
import ru.yandex.practicum.filmorate.exceptions.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;


@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaRatingStorage mpaRatingRepository;


    public Collection<MpaRating> findAll() {
        return mpaRatingRepository.findAll();
    }


    public MpaRating getMpaRatingById(int id) {
        return mpaRatingRepository.getMpaRatingById(id).orElseThrow(() -> new NotFoundException("Рейтинг с id = " + id + " не найден"));
    }


}
