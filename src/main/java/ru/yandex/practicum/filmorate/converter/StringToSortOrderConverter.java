package ru.yandex.practicum.filmorate.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.SortOrder;

@Component
public class StringToSortOrderConverter implements Converter<String, SortOrder> {
    @Override
    public SortOrder convert(String source) {
        return SortOrder.from(source);
    }
}