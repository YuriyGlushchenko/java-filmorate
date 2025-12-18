package ru.yandex.practicum.filmorate.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.SearchBy;

@Component
public class StringToSearchByConverter implements Converter<String, SearchBy> {
    @Override
    public SearchBy convert(String source) {
        return SearchBy.from(source);
    }
}