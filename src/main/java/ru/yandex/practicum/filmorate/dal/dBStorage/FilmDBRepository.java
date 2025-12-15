package ru.yandex.practicum.filmorate.dal.dBStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.dBStorage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SortOrder;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository("FilmDBRepository")
public class FilmDBRepository extends BaseRepository<Film> implements FilmStorage {


    private static final String FIND_ALL_QUERY = """
            SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration,
                   r.rating_id, r.rating_name
            FROM film f
            JOIN rating r ON r.rating_id = f.rating_id
            ORDER BY f.film_id
            """;

    private static final String INSERT_QUERY = "INSERT INTO film(film_name, description, release_date, duration, " + "rating_id) VALUES (?, ?, ?, ?, ?)";

    private static final String FIND_BY_ID_QUERY = """
            SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration,
                   r.rating_id, r.rating_name
            FROM film f
            JOIN rating r ON r.rating_id = f.rating_id
            WHERE f.film_id = ?
            """;

    private static final String REMOVE_BY_ID_QUERY = "DELETE FROM film WHERE film_id = ?;";

    private static final String UPDATE_QUERY = """
            UPDATE film
            SET film_name = ?,
                description = ?,
                release_date = ?,
                duration = ?,
                rating_id = ?
            WHERE film_id = ?
            """;

    private static final String ADD_LIKE_QUERY = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";

    private static final String REMOVE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?;";

    private static final String POPULAR_QUERY = """
            SELECT f.film_id,
                   f.film_name,
                   f.description,
                   f.release_date,
                   f.duration,
                   r.rating_id,
                   r.rating_name,
                   COUNT(l.user_id) AS likes_count
              FROM film f
              JOIN rating r ON r.rating_id = f.rating_id
              LEFT JOIN likes l ON l.film_id = f.film_id
             GROUP BY f.film_id,
                      f.film_name,
                      f.description,
                      f.release_date,
                      f.duration,
                      r.rating_id,
                      r.rating_name
             ORDER BY likes_count DESC,
                      f.film_id
             LIMIT ?
            """;

    private static final String FIND_BY_DIRECTOR_ID_QUERY_YEAR = """
            SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration,
                   r.rating_id, r.rating_name
            FROM film f
            JOIN rating r ON r.rating_id = f.rating_id
            JOIN films_directors fd ON fd.film_id = f.film_id
            WHERE fd.director_id = ?
            ORDER BY f.release_date
            """;

    private static final String FIND_BY_DIRECTOR_ID_QUERY_LIKES = """
            SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration,
                   r.rating_id, r.rating_name,
                   COUNT(l.user_id) AS likes_count
            FROM film f
            JOIN rating r ON r.rating_id = f.rating_id
            JOIN films_directors fd ON fd.film_id = f.film_id
            LEFT JOIN likes l ON l.film_id = f.film_id
            WHERE fd.director_id = ?
            GROUP BY f.film_id, f.film_name, f.description, f.release_date, f.duration, r.rating_id, r.rating_name
            ORDER BY likes_count DESC, f.film_id
            """;

    private static final String GET_FILM_RECOMENDATIONS_BY_USER_ID_QUERY = """
            SELECT
                f.film_id,
                f.film_name,
                f.description,
                f.release_date,
                f.duration,
                r.rating_id,
                r.rating_name,
                COUNT(DISTINCT similar_users.user_id) as recommended_by
            FROM likes l_other
            JOIN (
                SELECT l.user_id, COUNT(l.film_id) as common_likes
                FROM likes l
                JOIN likes l1 ON l.film_id = l1.film_id AND l1.user_id = ?
                WHERE l.user_id != ?
                GROUP BY l.user_id
                ORDER BY common_likes DESC
                LIMIT 10
            ) similar_users ON l_other.user_id = similar_users.user_id
            JOIN film f ON l_other.film_id = f.film_id
            JOIN rating r ON f.rating_id = r.rating_id
            WHERE NOT EXISTS (
                SELECT 1 FROM likes l2
                WHERE l2.user_id = ?
                AND l2.film_id = l_other.film_id
            )
            GROUP BY
                f.film_id,
                f.film_name,
                f.description,
                f.release_date,
                f.duration,
                r.rating_id,
                r.rating_name
            ORDER BY COUNT(DISTINCT similar_users.user_id) DESC
            LIMIT 20;
            """;

    private static final String GET_COMMON_FILMS = """
            SELECT *
              FROM FILM f
              JOIN (SELECT f.film_id,
              			   COUNT(user_id) as total_likes
              	      FROM FILM f
              	      JOIN LIKES l USING (film_id)
              	     GROUP BY f.film_id) USING (film_id)
              JOIN RATING r ON r.RATING_ID = f.RATING_ID
             WHERE f.film_id IN (SELECT film_id FROM LIKES WHERE user_id = ?
                                 INTERSECT
                                 SELECT film_id FROM LIKES WHERE user_id = ?)
             ORDER BY total_likes DESC;
            """;

    // Новая константа
    private static final String POPULAR_WITH_FILTERS_QUERY = """
            SELECT f.film_id,
                   f.film_name,
                   f.description,
                   f.release_date,
                   f.duration,
                   r.rating_id,
                   r.rating_name,
                   COUNT(l.user_id) AS likes_count
              FROM film f
              JOIN rating r ON r.rating_id = f.rating_id
              LEFT JOIN likes l ON l.film_id = f.film_id
             WHERE (?1 IS NULL OR EXISTS (
                    SELECT 1 FROM films_genre fg
                    WHERE f.film_id = fg.film_id AND fg.genre_id = ?1
                ))
               AND (?2 IS NULL OR EXTRACT(YEAR FROM f.release_date) = ?2)
             GROUP BY f.film_id, f.film_name, f.description, f.release_date,
                      f.duration, r.rating_id, r.rating_name
             ORDER BY likes_count DESC, f.film_id
             LIMIT ?3
            """;

    private static final String SEARCH_BY_TITLE_QUERY = """
            SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration,
                   r.rating_id, r.rating_name,
                   COUNT(l.user_id) AS likes_count
            FROM film f
            JOIN rating r ON r.rating_id = f.rating_id
            LEFT JOIN likes l ON l.film_id = f.film_id
            WHERE LOWER(f.film_name) LIKE LOWER(CONCAT('%', ?, '%'))
            GROUP BY f.film_id, f.film_name, f.description, f.release_date, f.duration
                     r.rating_id, r.rating_name
            ORDER BY likes_count DESC, f.film_id
            """;

    private static final String SEARCH_BY_DIRECTOR_QUERY = """
            SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration,
                   r.rating_id, r.rating_name,
                   COUNT(l.user_id) AS likes_count
            FROM film f
            JOIN rating r ON r.rating_id = f.rating_id
            JOIN films_directors fd ON f.film_id = fd.film_id
            JOIN director d ON fd.director_id = d.director_id
            LEFT JOIN likes l ON l.film_id = f.film_id
            WHERE LOWER(d.director_name) LIKE LOWER(CONCAT('%', ?, '%'))
            GROUP BY f.film_id, f.film_name, f.description, f.release_date, f.duration
                     r.rating_id, r.rating_name
            ORDER BY likes_count DESC, f.film_id
            """;

    private static final String SEARCH_BY_TITLE_AND_DIRECTOR_QUERY = """
            SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration,
                   r.rating_id, r.rating_name,
                   COUNT(l.user_id) AS likes_count
            FROM film f
            JOIN rating r ON r.rating_id = f.rating_id
            LEFT JOIN films_directors fd ON f.film_id = fd.film_id
            LEFT JOIN director d ON fd.director_id = d.director_id
            LEFT JOIN likes l ON l.film_id = f.film_id
            WHERE LOWER(f.film_name) LIKE LOWER(CONCAT('%', ?, '%'))
               OR LOWER(d.director_name) LIKE LOWER(CONCAT('%', ?, '%'))
            GROUP BY f.film_id, f.film_name, f.description, f.release_date, f.duration,
                     r.rating_id, r.rating_name
            ORDER BY likes_count DESC, f.film_id
            """;

    public FilmDBRepository(JdbcTemplate jdbc, FilmRowMapper filmRowMapper) {
        super(jdbc, filmRowMapper);
    }

    @Override
    public Collection<Film> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Film create(Film film) {

        int id = insert(INSERT_QUERY, film.getName(), film.getDescription(), java.sql.Date.valueOf(film.getReleaseDate()), film.getDuration(), film.getMpa().getId());

        film.setId(id);
        return film;
    }

    @Override
    public Optional<Film> getFilmById(int id) {

        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Film update(Film film) {
        update(UPDATE_QUERY, film.getName(), film.getDescription(), java.sql.Date.valueOf(film.getReleaseDate()), film.getDuration(), film.getMpa().getId(), film.getId());
        return film;
    }

    public void delete(int id) {
        delete(REMOVE_BY_ID_QUERY, id);
    }

    @Override
    public void addLike(int filmId, int userId) {
        update(ADD_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        update(REMOVE_LIKE_QUERY, filmId, userId);
    }

    @Override
    public Collection<Film> findMostPopular(int count) {
        return findMany(POPULAR_QUERY, count);
    }

    @Override
    public Collection<Film> findByDirectorId(int directorId, SortOrder sortOrder) {

        return switch (sortOrder) {
            case YEAR -> findMany(FIND_BY_DIRECTOR_ID_QUERY_YEAR, directorId);
            case LIKES -> findMany(FIND_BY_DIRECTOR_ID_QUERY_LIKES, directorId);
            default -> throw new IllegalArgumentException("Некорректный параметр сортировки: " + sortOrder);
        };
    }

    @Override
    public Collection<Film> getRecomendations(int userId) {
        return findMany(GET_FILM_RECOMENDATIONS_BY_USER_ID_QUERY, userId, userId, userId);
    }

    @Override
    public Collection<Film> getCommonFilms(int userId, int friendId) {
        return findMany(GET_COMMON_FILMS, userId, friendId);
    }

    // Новый метод
    @Override
    public Collection<Film> findMostPopular(int count, Integer genreId, Integer year) {
        return findMany(POPULAR_WITH_FILTERS_QUERY, genreId, year, count);
    }

    @Override
    public Collection<Film> searchFilms(String query, boolean searchByTitle, boolean searchByDirector) {
        if (!searchByTitle && !searchByDirector) {
            // Если не указано где искать, ищем везде
            return findMany(SEARCH_BY_TITLE_AND_DIRECTOR_QUERY, query, query);
        } else if (searchByTitle && searchByDirector) {
            // Ищем и по названию, и по режиссёру
            return findMany(SEARCH_BY_TITLE_AND_DIRECTOR_QUERY, query, query);
        } else if (searchByTitle) {
            // Только по названию
            return findMany(SEARCH_BY_TITLE_QUERY, query);
        } else {
            // Только по режиссёру
            return findMany(SEARCH_BY_DIRECTOR_QUERY, query);
        }
    }
}
