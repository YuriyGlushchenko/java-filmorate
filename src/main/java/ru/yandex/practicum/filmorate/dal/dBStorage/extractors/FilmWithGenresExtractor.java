package ru.yandex.practicum.filmorate.dal.dBStorage.extractors;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.dBStorage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class FilmWithGenresExtractor implements ResultSetExtractor<Collection<Film>> {
    private final FilmRowMapper filmRowMapper;

    public FilmWithGenresExtractor(FilmRowMapper filmRowMapper) {
        this.filmRowMapper = filmRowMapper;
    }

    @Override
    public Collection<Film> extractData(ResultSet rs) throws SQLException {
        // Map для хранения фильмов по ID
        Map<Integer, Film> films = new LinkedHashMap<>();

        // Вручную перебираем все строки ResultSet
        while (rs.next()) {
            int filmId = rs.getInt("film_id");
            Film film = films.get(filmId);

            // Если фильма еще нет в films, создаем его и добавляем в мапу
            if (film == null) {
                film = filmRowMapper.mapRow(rs, rs.getRow());
                film.setGenres(new HashSet<>()); // пока просто пустой
                films.put(filmId, film);
            }

            // Добавляем жанр, если он есть
            int genreId = rs.getInt("genre_id");
            if (!rs.wasNull()) {
                Genre genre = Genre.builder()
                        .id(genreId)
                        .name(rs.getString("genre_name"))
                        .build();
                film.getGenres().add(genre);
            }
        }

        return films.values();
    }
}