package ru.yandex.practicum.filmorate.dal.dBStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.GenreStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class GenreDBRepository extends BaseRepository<Genre> implements GenreStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genre";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genre WHERE genre_id = ?";
    private static final String FIND_FILM_GENRES_ID_QUERY = "SELECT fg.genre_id, g.genre_name FROM films_genre fg" +
            " JOIN genre g ON g.genre_id = fg.genre_id WHERE film_id = ?";

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
        String deleteSql = "DELETE FROM films_genre WHERE film_id = ?";
        jdbc.update(deleteSql, filmId);

        // Если список жанров не пустой, добавляем новые связи
        if (genres != null && !genres.isEmpty()) {
            String insertSql = "INSERT INTO films_genre (film_id, genre_id) VALUES (?, ?)";

            // Создаем batch запрос для эффективной вставки (т.е. записываем сразу все жанры за один запрос)
            List<Object[]> batchArgs = genres.stream()
                    .map(genre -> new Object[]{filmId, genre.getId()})
                    .collect(Collectors.toList());

            jdbc.batchUpdate(insertSql, batchArgs);
        }
    }


}
