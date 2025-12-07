package ru.yandex.practicum.filmorate.dal.dBStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.dBStorage.extractors.FilmWithGenresExtractor;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository("FilmDBRepository")
public class FilmDBRepository extends BaseRepository<Film> implements FilmStorage {
    private final FilmWithGenresExtractor filmWithGenresExtractor;

    private static final String FIND_ALL_WITH_GENRES_QUERY = """
            SELECT
                f.film_id, f.film_name, f.description, f.release_date, f.duration,
                r.rating_id, r.rating_name, g.genre_id, g.genre_name
            FROM film f
            JOIN rating r ON r.rating_id = f.rating_id
            LEFT JOIN films_genre fg ON fg.film_id = f.film_id
            LEFT JOIN genre g ON g.genre_id = fg.genre_id
            ORDER BY f.film_id, g.genre_id
            """;

    private static final String INSERT_QUERY = "INSERT INTO film(film_name, description, release_date, duration, " +
            "rating_id) VALUES (?, ?, ?, ?, ?)";

    private static final String FIND_BY_ID_QUERY = "SELECT * FROM film f JOIN rating r ON r.rating_id = f.rating_id" +
            " WHERE film_id = ?";

    private static final String UPDATE_QUERY = "UPDATE film SET film_name = ?, description = ?, release_date = ?," +
            " duration = ?, rating_id =? WHERE film_id = ?";

    private static final String ADD_LIKE_QUERY = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";

    private static final String REMOVE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?;";

    private static final String POPULAR_WITH_GENRES_QUERY = """
            SELECT
                f.film_id, f.film_name, f.description, f.release_date, f.duration,
                r.rating_id, r.rating_name, g.genre_id, g.genre_name,
                film_likes.likes_count
            FROM (
                SELECT
                    l.film_id,
                    COUNT(l.user_id) AS likes_count
                FROM likes l
                GROUP BY l.film_id
                ORDER BY likes_count DESC
                LIMIT ?
            ) AS film_likes
            JOIN film f ON f.film_id = film_likes.film_id
            JOIN rating r ON r.rating_id = f.rating_id
            LEFT JOIN films_genre fg ON fg.film_id = f.film_id
            LEFT JOIN genre g ON g.genre_id = fg.genre_id
            ORDER BY film_likes.likes_count DESC, f.film_id, g.genre_id
            """;

    public FilmDBRepository(JdbcTemplate jdbc, RowMapper<Film> mapper, FilmWithGenresExtractor filmWithGenresExtractor) {
        super(jdbc, mapper);
        this.filmWithGenresExtractor = filmWithGenresExtractor;
    }

    @Override
    public Collection<Film> findAll() {
        return jdbc.query(FIND_ALL_WITH_GENRES_QUERY, filmWithGenresExtractor);
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
        return jdbc.query(POPULAR_WITH_GENRES_QUERY, filmWithGenresExtractor, count);
    }

}
