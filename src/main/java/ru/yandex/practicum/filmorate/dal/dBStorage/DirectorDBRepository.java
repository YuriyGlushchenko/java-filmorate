package ru.yandex.practicum.filmorate.dal.dBStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.DirectorStorage;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository("DirectorDBRepository")
public class DirectorDBRepository extends BaseRepository<Director> implements DirectorStorage {
    private static final String FIND_ALL_DIRECTORS_QUERY = "SELECT * FROM director";

    private static final String FIND_DIRECTOR_BY_ID_QUERY = "SELECT * FROM director WHERE director_id = ?";

    private static final String INSERT_QUERY = "INSERT INTO director(director_name) VALUES (?)";

    private static final String UPDATE_QUERY = "UPDATE director SET director_name = ? WHERE director_id = ?";

    private static final String DELETE_FRIEND_QUERY = "DELETE FROM director WHERE director_id = ?";

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

        int id = insert(
                INSERT_QUERY,
                director.getName()
        );

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
        delete(DELETE_FRIEND_QUERY, id);
    }
}
