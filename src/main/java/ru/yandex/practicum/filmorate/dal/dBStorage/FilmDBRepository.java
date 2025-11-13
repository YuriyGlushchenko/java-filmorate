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

    private static final String ADD_LIKE_QUERY = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";

    private static final String REMOVE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?;";

    private static final String POPULAR_QUERY = "SELECT l.film_id, f.film_name, f.description, f.release_date, " +
            "f.duration, f.rating_id, COUNT(l.user_id) AS likes_count FROM likes l LEFT JOIN film f ON f.film_id = l.film_id" +
            " GROUP BY l.film_id ORDER BY likes_count DESC LIMIT ?;";

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

        return findOne(FIND_BY_ID_QUERY, id);
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

    @Override
    public void addLike(int filmId, int userId) {
        update(ADD_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        update(REMOVE_LIKE_QUERY, filmId, userId);
    }

    public Collection<Film> findMostPopular(int count) {
        return findMany(POPULAR_QUERY, count);
    }

}
