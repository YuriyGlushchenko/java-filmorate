package ru.yandex.practicum.filmorate.FilmoRateApplication;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.GenreStorage;
import ru.yandex.practicum.filmorate.dal.dBStorage.GenreDBRepository;
import ru.yandex.practicum.filmorate.dal.dBStorage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({GenreDBRepository.class, GenreRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDBRepositoryTest {
    private final GenreStorage genreRepository;

    @Test
    public void testFindAllGenres() {
        List<Genre> allGenres = genreRepository.findAll();

        assertThat(allGenres).isNotNull();
        assertThat(allGenres.size() == 6).isTrue(); // Из data.sql 6 жанров

        List<String> genreNames = allGenres.stream()
                .map(Genre::getName)
                .toList();

        assertThat(genreNames.contains("Комедия")).isTrue();
        assertThat(genreNames.contains("Драма")).isTrue();
        assertThat(genreNames.contains("Мультфильм")).isTrue();
        assertThat(genreNames.contains("Триллер")).isTrue();
        assertThat(genreNames.contains("Документальный")).isTrue();
        assertThat(genreNames.contains("Боевик")).isTrue();

        // Проверяем, что жанры имеют корректные ID
        Optional<Genre> comedy = allGenres.stream()
                .filter(g -> g.getName().equals("Комедия"))
                .findFirst();
        assertThat(comedy.isPresent()).isTrue();
        assertThat(comedy.get().getId()).isEqualTo(1);

        Optional<Genre> drama = allGenres.stream()
                .filter(g -> g.getName().equals("Драма"))
                .findFirst();
        assertThat(drama.isPresent()).isTrue();
        assertThat(drama.get().getId()).isEqualTo(2);
    }

    @Test
    public void testGetGenreById() {
        Optional<Genre> genreOptional = genreRepository.getGenreById(1);

        assertThat(genreOptional.isPresent()).isTrue();
        Genre genre = genreOptional.get();
        assertThat(genre.getId()).isEqualTo(1);
        assertThat(genre.getName()).isEqualTo("Комедия");

        Optional<Genre> genreOptional2 = genreRepository.getGenreById(2);
        assertThat(genreOptional2.isPresent()).isTrue();
        assertThat(genreOptional2.get().getName()).isEqualTo("Драма");
    }

    @Test
    public void testGetGenreByNotValidId() {
        Optional<Genre> genreOptional = genreRepository.getGenreById(999);
        assertThat(genreOptional.isPresent()).isFalse();
    }

    @Test
    public void testGetFilmGenresByFilmId() {
        // Из data.sql: фильм 1 (Матрица) имеет жанры: Боевик (6), Триллер (4)
        List<Genre> filmGenres = genreRepository.getFilmGenresByFilmId(1);

        assertThat(filmGenres).isNotNull();
        assertThat(filmGenres.size() == 2).isTrue();

        List<String> genreNames = filmGenres.stream()
                .map(Genre::getName)
                .toList();

        assertThat(genreNames.contains("Боевик")).isTrue();
        assertThat(genreNames.contains("Триллер")).isTrue();

        // Проверяем, что жанры имеют корректные данные
        filmGenres.forEach(genre -> {
            assertThat(genre.getId()).isNotNull();
            assertThat(genre.getName()).isNotNull();
        });
    }

    @Test
    public void testGetFilmGenresForFilmWithoutGenres() {
        // Создаем новый фильм без жанров (виртуально)
        List<Genre> filmGenres = genreRepository.getFilmGenresByFilmId(999);
        assertThat(filmGenres).isNotNull();
        assertThat(filmGenres.size() == 0).isTrue();
    }

    @Test
    public void testSaveFilmGenres() {
        // Создаем набор жанров для сохранения
        Set<Genre> genresToSave = Set.of(
                Genre.builder().id(1).name("Комедия").build(),
                Genre.builder().id(3).name("Мультфильм").build(),
                Genre.builder().id(5).name("Документальный").build()
        );

        // Сохраняем жанры для фильма (используем существующий фильм из data.sql)
        int filmId = 1; // Матрица
        genreRepository.saveFilmGenres(filmId, genresToSave);

        // Проверяем, что жанры сохранились
        List<Genre> savedGenres = genreRepository.getFilmGenresByFilmId(filmId);
        assertThat(savedGenres).isNotNull();
        assertThat(savedGenres.size() == 3).isTrue();

        List<String> savedGenreNames = savedGenres.stream()
                .map(Genre::getName)
                .toList();

        assertThat(savedGenreNames.contains("Комедия")).isTrue();
        assertThat(savedGenreNames.contains("Мультфильм")).isTrue();
        assertThat(savedGenreNames.contains("Документальный")).isTrue();
    }

    @Test
    public void testSaveFilmGenresWithEmptySet() {
        // Сохраняем пустой набор жанров (очищаем жанры фильма)
        int filmId = 1; // Матрица
        genreRepository.saveFilmGenres(filmId, Set.of());

        // Проверяем, что жанры очистились
        List<Genre> filmGenres = genreRepository.getFilmGenresByFilmId(filmId);
        assertThat(filmGenres).isNotNull();
        assertThat(filmGenres.isEmpty()).isTrue();
    }

    @Test
    public void testSaveFilmGenresWithNull() {
        // Сохраняем null вместо набора жанров (должно очистить жанры)
        int filmId = 1; // Матрица
        genreRepository.saveFilmGenres(filmId, null);

        // Проверяем, что ошибки нет, список жанров пуст
        List<Genre> filmGenres = genreRepository.getFilmGenresByFilmId(filmId);
        assertThat(filmGenres).isNotNull();
        assertThat(filmGenres.isEmpty()).isTrue();
    }

    @Test
    public void testSaveFilmGenresOverwriteExisting() {
        // Изначально у фильма 1 есть жанры: Боевик, Триллер
        List<Genre> initialGenres = genreRepository.getFilmGenresByFilmId(1);
        assertThat(initialGenres.size() == 2).isTrue();

        // Сохраняем новые жанры
        Set<Genre> newGenres = Set.of(
                Genre.builder().id(1).name("Комедия").build(),
                Genre.builder().id(2).name("Драма").build()
        );
        genreRepository.saveFilmGenres(1, newGenres);

        // Проверяем, что старые жанры заменены новыми
        List<Genre> updatedGenres = genreRepository.getFilmGenresByFilmId(1);
        assertThat(updatedGenres.size() == 2).isTrue();

        List<String> updatedGenreNames = updatedGenres.stream()
                .map(Genre::getName)
                .collect(Collectors.toList());

        assertThat(updatedGenreNames.contains("Комедия")).isTrue();
        assertThat(updatedGenreNames.contains("Драма")).isTrue();
        assertThat(updatedGenreNames.contains("Боевик")).isFalse(); // Старый жанр удален
        assertThat(updatedGenreNames.contains("Триллер")).isFalse(); // Старый жанр удален
    }

    @Test
    public void testGenreDataConsistency() {
        // Проверяем, что данные из БД соответствуют ожидаемым
        Optional<Genre> genre1 = genreRepository.getGenreById(1);
        assertThat(genre1.isPresent()).isTrue();
        assertThat(genre1.get().getId()).isEqualTo(1);
        assertThat(genre1.get().getName()).isEqualTo("Комедия");

        Optional<Genre> genre2 = genreRepository.getGenreById(2);
        assertThat(genre2.isPresent()).isTrue();
        assertThat(genre2.get().getId()).isEqualTo(2);
        assertThat(genre2.get().getName()).isEqualTo("Драма");

        Optional<Genre> genre3 = genreRepository.getGenreById(3);
        assertThat(genre3.isPresent()).isTrue();
        assertThat(genre3.get().getId()).isEqualTo(3);
        assertThat(genre3.get().getName()).isEqualTo("Мультфильм");

        Optional<Genre> genre4 = genreRepository.getGenreById(4);
        assertThat(genre4.isPresent()).isTrue();
        assertThat(genre4.get().getId()).isEqualTo(4);
        assertThat(genre4.get().getName()).isEqualTo("Триллер");

        Optional<Genre> genre5 = genreRepository.getGenreById(5);
        assertThat(genre5.isPresent()).isTrue();
        assertThat(genre5.get().getId()).isEqualTo(5);
        assertThat(genre5.get().getName()).isEqualTo("Документальный");

        Optional<Genre> genre6 = genreRepository.getGenreById(6);
        assertThat(genre6.isPresent()).isTrue();
        assertThat(genre6.get().getId()).isEqualTo(6);
        assertThat(genre6.get().getName()).isEqualTo("Боевик");
    }

    @Test
    public void testMultipleFilmGenres() {
        // Проверяем жанры для разных фильмов из data.sql
        List<Genre> film2Genres = genreRepository.getFilmGenresByFilmId(2); // Король Лев
        assertThat(film2Genres.size() == 2).isTrue();

        List<String> film2GenreNames = film2Genres.stream()
                .map(Genre::getName)
                .toList();
        assertThat(film2GenreNames.contains("Мультфильм")).isTrue();
        assertThat(film2GenreNames.contains("Драма")).isTrue();

        List<Genre> film6Genres = genreRepository.getFilmGenresByFilmId(6); // Ирония судьбы
        assertThat(film6Genres.size() == 2).isTrue();

        List<String> film6GenreNames = film6Genres.stream()
                .map(Genre::getName)
                .toList();
        assertThat(film6GenreNames.contains("Комедия")).isTrue();
        assertThat(film6GenreNames.contains("Драма")).isTrue();
    }

}
