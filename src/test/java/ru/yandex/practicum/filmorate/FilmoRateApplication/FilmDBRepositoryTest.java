package ru.yandex.practicum.filmorate.FilmoRateApplication;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.dBStorage.FilmDBRepository;
import ru.yandex.practicum.filmorate.dal.dBStorage.extractors.FilmWithGenresExtractor;
import ru.yandex.practicum.filmorate.dal.dBStorage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exceptions.exceptions.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDBRepository.class, FilmRowMapper.class, FilmWithGenresExtractor.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDBRepositoryTest {
    private final FilmStorage filmRepository;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        MpaRating mpa = MpaRating.builder()
                .id(1)
                .name("G")
                .build();

        testFilm = Film.builder()
                .name("Новый тестовый фильм")
                .description("Описание тестового фильма")
                .releaseDate(LocalDate.of(2020, 5, 15))
                .duration(120)
                .mpa(mpa)
                .build();
    }

    @Test
    public void testFindFilmById() {
        Optional<Film> filmOptional = filmRepository.getFilmById(1);

        assertThat(filmOptional.isPresent()).isTrue();

        Film film = filmOptional.get();

        assertThat(film.getId()).isEqualTo(1);
        assertThat(film.getName()).isEqualTo("Матрица");
        assertThat(film.getDescription()).isEqualTo("Хакер Нео discovers the truth about his reality");
        assertThat(film.getDuration()).isEqualTo(136);
        assertThat(film.getMpa()).isNotNull();
        assertThat(film.getMpa().getId()).isEqualTo(1);
    }

    @Test
    public void testFindFilmByNotValidId() {
        Optional<Film> filmOptional = filmRepository.getFilmById(999);
        assertThat(filmOptional.isPresent()).isFalse();
    }

    @Test
    public void testFindAllFilms() {
        Collection<Film> allFilms = filmRepository.findAll();

        assertThat(allFilms).isNotNull();
        assertThat(allFilms.size() == 6).isTrue(); // Из data.sql 6 фильмов

        List<String> filmNames = allFilms.stream()
                .map(Film::getName)
                .toList();

        assertThat(filmNames.contains("Матрица")).isTrue();
        assertThat(filmNames.contains("Король Лев")).isTrue();
        assertThat(filmNames.contains("Начало")).isTrue();

        Optional<Film> matrix = allFilms.stream()
                .filter(f -> f.getName().equals("Матрица"))
                .findFirst();
        assertThat(matrix.isPresent()).isTrue();

        Optional<Film> lionKing = allFilms.stream()
                .filter(f -> f.getName().equals("Король Лев"))
                .findFirst();
        assertThat(lionKing.isPresent()).isTrue();
    }

    @Test
    public void testCreateFilm() {
        Film createdFilm = filmRepository.create(testFilm);

        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId() > 0).isTrue();
        assertThat(createdFilm.getName()).isEqualTo("Новый тестовый фильм");
        assertThat(createdFilm.getDescription()).isEqualTo("Описание тестового фильма");
        assertThat(createdFilm.getReleaseDate()).isEqualTo(LocalDate.of(2020, 5, 15));
        assertThat(createdFilm.getDuration()).isEqualTo(120);
        assertThat(createdFilm.getMpa().getId()).isEqualTo(1);

        Optional<Film> retrievedFilm = filmRepository.getFilmById(createdFilm.getId());
        assertThat(retrievedFilm.isPresent()).isTrue();
        assertThat(retrievedFilm.get().getName()).isEqualTo("Новый тестовый фильм");
    }

    @Test
    public void testUpdateFilm() {
        Film createdFilm = filmRepository.create(testFilm);
        int filmId = createdFilm.getId();

        MpaRating newMpa = MpaRating.builder()
                .id(2)
                .name("PG")
                .build();

        Film updatedFilm = Film.builder()
                .id(filmId)
                .name("Обновленное название")
                .description("Обновленное описание")
                .releaseDate(LocalDate.of(2021, 6, 20))
                .duration(150)
                .mpa(newMpa)
                .build();

        Film result = filmRepository.update(updatedFilm);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(filmId);
        assertThat(result.getName()).isEqualTo("Обновленное название");
        assertThat(result.getDescription()).isEqualTo("Обновленное описание");
        assertThat(result.getReleaseDate()).isEqualTo(LocalDate.of(2021, 6, 20));
        assertThat(result.getDuration()).isEqualTo(150);
        assertThat(result.getMpa().getId()).isEqualTo(2);

        Optional<Film> retrievedFilm = filmRepository.getFilmById(filmId);
        assertThat(retrievedFilm.isPresent()).isTrue();
        assertThat(retrievedFilm.get().getName()).isEqualTo("Обновленное название");
        assertThat(retrievedFilm.get().getMpa().getId()).isEqualTo(2);
    }

    @Test
    public void testAddLike() {
        // Сначала создаем фильм
        Film createdFilm = filmRepository.create(testFilm);
        int filmId = createdFilm.getId();

        // Добавляем лайк от пользователя 1
        filmRepository.addLike(filmId, 1);

        // Проверяем, что лайк добавлен (косвенно через популярные фильмы)

        Collection<Film> popularFilms = filmRepository.findMostPopular(10);
        boolean filmIsPopular = popularFilms.stream()
                .anyMatch(film -> film.getId() == filmId);
        assertThat(filmIsPopular).isTrue();
    }

    @Test
    public void testRemoveLike() {
        // Сначала создаем фильм и добавляем лайк
        Film createdFilm = filmRepository.create(testFilm);
        int filmId = createdFilm.getId();
        filmRepository.addLike(filmId, 1);

        // Удаляем лайк
        filmRepository.removeLike(filmId, 1);

        // Проверяем, что операция выполнилась без ошибок
        assertThat(true).isTrue();
    }

    @Test
    public void testFindMostPopularFilms() {
        // Из data.sql фильмы с лайками
        Collection<Film> popularFilms = filmRepository.findMostPopular(3);

        assertThat(popularFilms).isNotNull();
        assertThat(popularFilms.size() == 3).isTrue();

        // Проверяем что все фильмы имеют корректные данные
        popularFilms.forEach(film -> {
            assertThat(film.getName()).isNotNull();
            assertThat(film.getDescription()).isNotNull();
            assertThat(film.getDuration() > 0).isTrue();
            assertThat(film.getMpa()).isNotNull();
        });
    }

    @Test
    public void testFindMostPopularWithLimit() {
        // Тестируем разное количество возвращаемых фильмов
        Collection<Film> top1 = filmRepository.findMostPopular(1);
        assertThat(top1.size() == 1).isTrue();

        Collection<Film> top5 = filmRepository.findMostPopular(5);
        assertThat(top5.size() == 5).isTrue();

        Collection<Film> top10 = filmRepository.findMostPopular(10);
        assertThat(top10.size() >= 6).isTrue(); // Все фильмы из data.sql
    }

    @Test
    public void testFilmDataConsistency() {
        Optional<Film> film1 = filmRepository.getFilmById(1);
        assertThat(film1.isPresent()).isTrue();
        assertThat(film1.get().getName()).isEqualTo("Матрица");
        assertThat(film1.get().getDescription()).isEqualTo("Хакер Нео discovers the truth about his reality");
        assertThat(film1.get().getReleaseDate()).isEqualTo(LocalDate.of(1999, 3, 31));
        assertThat(film1.get().getDuration()).isEqualTo(136);
        assertThat(film1.get().getMpa().getId()).isEqualTo(1);

        Optional<Film> film2 = filmRepository.getFilmById(2);
        assertThat(film2.isPresent()).isTrue();
        assertThat(film2.get().getName()).isEqualTo("Король Лев");
        assertThat(film2.get().getDescription()).isEqualTo("Молодой лев Симба борется за свое королевство");
        assertThat(film2.get().getReleaseDate()).isEqualTo(LocalDate.of(1994, 6, 24));
        assertThat(film2.get().getDuration()).isEqualTo(88);
        assertThat(film2.get().getMpa().getId()).isEqualTo(1);
    }

    @Test
    public void testCreateAndRetrieveMultipleFilms() {
        Film film1 = Film.builder()
                .name("Тестовый фильм 1")
                .description("Описание 1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(100)
                .mpa(createMpa(1))
                .build();

        Film film2 = Film.builder()
                .name("Тестовый фильм 2")
                .description("Описание 2")
                .releaseDate(LocalDate.of(2021, 2, 2))
                .duration(110)
                .mpa(createMpa(2))
                .build();

        Film created1 = filmRepository.create(film1);
        Film created2 = filmRepository.create(film2);

        assertThat(created1.getId() != created2.getId()).isTrue();

        Collection<Film> allFilms = filmRepository.findAll();
        assertThat(allFilms.size() >= 8).isTrue(); // 6 из data.sql + 2 новых

        List<String> filmNames = allFilms.stream()
                .map(Film::getName)
                .collect(Collectors.toList());

        assertThat(filmNames.contains("Тестовый фильм 1")).isTrue();
        assertThat(filmNames.contains("Тестовый фильм 2")).isTrue();
    }

    @Test
    public void testUpdateFilmWithSameData() {
        Film createdFilm = filmRepository.create(testFilm);
        int filmId = createdFilm.getId();

        Film sameDataFilm = Film.builder()
                .id(filmId)
                .name("Новый тестовый фильм")
                .description("Описание тестового фильма")
                .releaseDate(LocalDate.of(2020, 5, 15))
                .duration(120)
                .mpa(createMpa(1))
                .build();

        Film result = filmRepository.update(sameDataFilm);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(filmId);
        assertThat(result.getName()).isEqualTo("Новый тестовый фильм");

        Optional<Film> retrievedFilm = filmRepository.getFilmById(filmId);
        assertThat(retrievedFilm.isPresent()).isTrue();
        assertThat(retrievedFilm.get().getName()).isEqualTo("Новый тестовый фильм");
    }

    @Test
    public void testAddLikeToNonExistentFilm() {
        // При нарушении foreign key constraint выбрасывается DataIntegrityViolationException
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
            filmRepository.addLike(888, 1);
        });
    }

    @Test
    public void testRemoveLikeFromNonExistentFilm() {
        // Попытка удалить лайк несуществующему фильму должна вызывать исключение
        assertThrows(InternalServerException.class, () -> {
            filmRepository.removeLike(999, 1);
        });
    }

    @Test
    public void testFilmWithDifferentMpaRatings() {
        // Тестируем создание фильмов с разными рейтингами
        Film filmWithPg = Film.builder()
                .name("Фильм с PG")
                .description("Тест")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(100)
                .mpa(createMpa(2))
                .build();

        Film filmWithPg13 = Film.builder()
                .name("Фильм с PG-13")
                .description("Тест")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(100)
                .mpa(createMpa(3))
                .build();

        Film created1 = filmRepository.create(filmWithPg);
        Film created2 = filmRepository.create(filmWithPg13);

        assertThat(created1.getMpa().getId()).isEqualTo(2);
        assertThat(created2.getMpa().getId()).isEqualTo(3);
    }

    private MpaRating createMpa(int id) {
        return MpaRating.builder()
                .id(id)
                .name(getMpaNameById(id))
                .build();
    }

    private String getMpaNameById(int id) {
        switch (id) {
            case 1:
                return "G";
            case 2:
                return "PG";
            case 3:
                return "PG-13";
            case 4:
                return "R";
            case 5:
                return "NC-17";
            default:
                return "G";
        }
    }
}