package ru.yandex.practicum.filmorate.dal.dBStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.DirectorStorage;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository("DirectorDBRepository")
public class DirectorDBRepository extends BaseRepository<Director> implements DirectorStorage {
    private static final String FIND_ALL_DIRECTORS_QUERY = "SELECT * FROM director";

    private static final String FIND_DIRECTOR_BY_ID_QUERY = "SELECT * FROM director WHERE director_id = ?";

    private static final String INSERT_QUERY = "INSERT INTO director(director_name) VALUES (?)";

    private static final String UPDATE_QUERY = "UPDATE director SET director_name = ? WHERE director_id = ?";

    private static final String DELETE_DIRECTOR_QUERY = "DELETE FROM director WHERE director_id = ?";

    private static final String SAVE_FILM_DIRECTORS_QUERY = "INSERT INTO films_directors (film_id, director_id) VALUES (?, ?)";

    private static final String DELETE_FILM_DIRECTORS_QUERY = "DELETE FROM films_directors WHERE film_id = ?";

    private static final String FIND_FILM_DIRECTORS_QUERY =
            "SELECT d.* FROM director d JOIN films_directors fd ON d.director_id = fd.director_id WHERE fd.film_id = ?";

    public DirectorDBRepository(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Director> findAll() {
        return findMany(FIND_ALL_DIRECTORS_QUERY);
    }

    @Override
    public Optional<Director> getDirectorById(int id) {
        return findOne(FIND_DIRECTOR_BY_ID_QUERY, id);
    }

    @Override
    public Director create(Director director) {
        int id = insert(INSERT_QUERY, director.getName());
        director.setId(id);

        return director;
    }

    @Override
    public Director update(Director director) {

        update(
                UPDATE_QUERY,
                director.getName(),
                director.getId()
        );
        return director;
    }

    @Override
    public void delete(int id) {
        delete(DELETE_DIRECTOR_QUERY, id);
    }

    @Override
    public void saveFilmDirectors(int filmId, Set<Director> directors) {
        // Сначала удаляем старых режиссеров
        deleteFilmDirectors(filmId);

        // Затем добавляем новых
        if (directors != null && !directors.isEmpty()) {
            List<Object[]> batchArgs = directors.stream()
                    .map(director -> new Object[]{filmId, director.getId()})
                    .collect(Collectors.toList());

            jdbc.batchUpdate(SAVE_FILM_DIRECTORS_QUERY, batchArgs);
        }
    }

    @Override
    public void deleteFilmDirectors(int filmId) {
        jdbc.update(DELETE_FILM_DIRECTORS_QUERY, filmId);
    }

    @Override
    public Set<Director> getFilmDirectorsByFilmId(int filmId) {
        return new HashSet<>(findMany(FIND_FILM_DIRECTORS_QUERY, filmId));
    }

    @Override
    public Map<Integer, Set<Director>> getFilmDirectorsForFilms(Collection<Integer> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return new HashMap<>();
        }

        // Создаём строку из нужного количества плейсхолдеров (строка вида '?, ?, ?, ?' по числу элементов в filmIds)
        String placeholders = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        // Вставляем плейсхолдеры в запрос
        String query =
                "SELECT fd.film_id, d.director_id, d.director_name " +
                        "FROM director d " +
                        "JOIN films_directors fd ON d.director_id = fd.director_id " +
                        "WHERE fd.film_id IN (" + placeholders + ")";

        // Создаем массив параметров
        Object[] params = filmIds.toArray();

        // Выполняем запрос с параметрами
        return jdbc.query(query, rs -> {
            Map<Integer, Set<Director>> result = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                Director director = Director.builder()
                        .id(rs.getInt("director_id"))
                        .name(rs.getString("director_name"))
                        .build();
                result.computeIfAbsent(filmId, k -> new HashSet<>()).add(director);
            }
            return result;
        }, params);
    }

    @Override
    public Set<Director> getDirectorsByIds(Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }

        // Создаём строку из нужного количества плейсхолдеров
        String placeholders = ids.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String query = String.format(
                "SELECT * FROM director WHERE director_id IN (%s)",
                placeholders
        );

        List<Director> directors = findMany(query, ids.toArray());
        return new HashSet<>(directors);
    }
}