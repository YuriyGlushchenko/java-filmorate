package ru.yandex.practicum.filmorate.dal.dBStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.GenreStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class GenreDBRepository extends BaseRepository<Genre> implements GenreStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genre";

    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genre WHERE genre_id = ?";

    private static final String FIND_FILM_GENRES_ID_QUERY = "SELECT g.genre_id, g.genre_name FROM films_genre fg " +
            "JOIN genre g ON g.genre_id = fg.genre_id " +
            "WHERE film_id = ? " +
            "ORDER BY g.genre_id ASC";
    ;

    private static final String ADD_GENRES_QUERY = "INSERT INTO films_genre (film_id, genre_id) VALUES (?, ?)";

    private static final String CLEAN_GENRES_QUERY = "DELETE FROM films_genre WHERE film_id = ?";

    private static final String FIND_GENRES_FOR_FILMS_QUERY =
            "SELECT fg.film_id, g.genre_id, g.genre_name " +
                    "FROM genre g " +
                    "JOIN films_genre fg ON g.genre_id = fg.genre_id " +
                    "WHERE fg.film_id IN (%s)" +
                    "ORDER BY g.genre_id ASC";

    public GenreDBRepository(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Genre> getGenreById(int id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public List<Genre> getFilmGenresByFilmId(int filmId) {
        return findMany(FIND_FILM_GENRES_ID_QUERY, filmId);
    }

    @Override
    public void saveFilmGenres(int filmId, Set<Genre> genres) {
        // Сначала удаляем все существующие жанры для этого фильма
        jdbc.update(CLEAN_GENRES_QUERY, filmId);

        // Если список жанров не пустой, добавляем новые связи
        if (genres != null && !genres.isEmpty()) {
            // Создаем batch запрос для эффективной вставки (т.е. записываем сразу все жанры за один запрос)
            List<Object[]> batchArgs = genres.stream()
                    .map(genre -> new Object[]{filmId, genre.getId()})
                    .collect(Collectors.toList());

            jdbc.batchUpdate(ADD_GENRES_QUERY, batchArgs);
        }
    }

    @Override
    public Map<Integer, Set<Genre>> getFilmGenresForFilms(Collection<Integer> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return new HashMap<>();
        }

        // Создаем строку с перечислением ID через запятую
        String inClause = filmIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        // Формируем SQL-запрос
        String query = String.format(FIND_GENRES_FOR_FILMS_QUERY, inClause);

        // Выполняем запрос и собираем результаты
        return jdbc.query(query, rs -> {
            Map<Integer, Set<Genre>> result = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                Genre genre = Genre.builder()
                        .id(rs.getInt("genre_id"))
                        .name(rs.getString("genre_name"))
                        .build();

                result.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
            }
            return result;
        });
    }
}
