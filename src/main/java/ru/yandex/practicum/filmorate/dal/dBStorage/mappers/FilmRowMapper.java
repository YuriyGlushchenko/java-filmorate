package ru.yandex.practicum.filmorate.dal.dBStorage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {

        Film.FilmBuilder filmBuilder = Film.builder()
                .id(resultSet.getInt("film_id"))
                .name(resultSet.getString("film_name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getObject("release_date", LocalDate.class))
                .duration(resultSet.getInt("duration"));

        try {
            int likesCount = resultSet.getInt("likes_count");
            if (!resultSet.wasNull()) {
                filmBuilder.likesCount(likesCount);
            }
        } catch (SQLException e) {
            // Если поля нет в ResultSet, оставляем значение по умолчанию (0)
        }

        Film film = filmBuilder.build();

        MpaRating mpa = MpaRating.builder()
                .id(resultSet.getInt("rating_id"))
                .name(resultSet.getString("rating_name"))
                .build();

        film.setMpa(mpa);

        return film;
    }
}