package ru.yandex.practicum.filmorate.dal.dBStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository("FilmDBRepository")
public class FilmDBRepository extends BaseRepository<Film> implements FilmStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM film";
    private static final String INSERT_QUERY = "INSERT INTO film(film_name, description, release_date, duration, " +
            "rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM film WHERE film_id = ?";
    private static final String UPDATE_QUERY = "UPDATE film SET film_name = ?, description = ?, release_date = ?," +
            " duration = ?, rating_id =? WHERE film_id = ?";

    public FilmDBRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Film> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Film create(Film film) {

        int id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId()
        );

        film.setId(id);
        return film;
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        Optional<Film> filmOptional = findOne(FIND_BY_ID_QUERY, id);
//        filmOptional.ifPresent(this::loadUserFriends); // меняем данные User ВНУТРИ Optional, потом его же и возвращаем

        return filmOptional;

    }

    @Override
    public Film update(Film film) {

        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        return film;
    }


}
