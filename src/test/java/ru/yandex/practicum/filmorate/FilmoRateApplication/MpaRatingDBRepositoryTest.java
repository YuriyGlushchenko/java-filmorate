package ru.yandex.practicum.filmorate.FilmoRateApplication;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.MpaRatingStorage;
import ru.yandex.practicum.filmorate.dal.dBStorage.MpaRatingDBRepository;
import ru.yandex.practicum.filmorate.dal.dBStorage.mappers.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaRatingDBRepository.class, MpaRatingRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaRatingDBRepositoryTest {
    private final MpaRatingStorage mpaRatingRepository;

    @Test
    public void testFindAllMpaRatings() {
        List<MpaRating> allMpaRatings = mpaRatingRepository.findAll();

        assertThat(allMpaRatings).isNotNull();
        assertThat(allMpaRatings.size() == 5).isTrue(); // Из data.sql 5 рейтингов

        List<String> mpaNames = allMpaRatings.stream()
                .map(MpaRating::getName)
                .toList();

        assertThat(mpaNames.contains("G")).isTrue();
        assertThat(mpaNames.contains("PG")).isTrue();
        assertThat(mpaNames.contains("PG-13")).isTrue();
        assertThat(mpaNames.contains("R")).isTrue();
        assertThat(mpaNames.contains("NC-17")).isTrue();

        // Проверяем, что рейтинги имеют корректные ID
        Optional<MpaRating> gRating = allMpaRatings.stream()
                .filter(mpa -> mpa.getName().equals("G"))
                .findFirst();
        assertThat(gRating.isPresent()).isTrue();
        assertThat(gRating.get().getId()).isEqualTo(1);

        Optional<MpaRating> pgRating = allMpaRatings.stream()
                .filter(mpa -> mpa.getName().equals("PG"))
                .findFirst();
        assertThat(pgRating.isPresent()).isTrue();
        assertThat(pgRating.get().getId()).isEqualTo(2);
    }

    @Test
    public void testGetMpaRatingById() {
        Optional<MpaRating> mpaOptional = mpaRatingRepository.getMpaRatingById(1);

        assertThat(mpaOptional.isPresent()).isTrue();
        MpaRating mpa = mpaOptional.get();
        assertThat(mpa.getId()).isEqualTo(1);
        assertThat(mpa.getName()).isEqualTo("G");

        Optional<MpaRating> mpaOptional2 = mpaRatingRepository.getMpaRatingById(2);
        assertThat(mpaOptional2.isPresent()).isTrue();
        assertThat(mpaOptional2.get().getName()).isEqualTo("PG");

        Optional<MpaRating> mpaOptional3 = mpaRatingRepository.getMpaRatingById(3);
        assertThat(mpaOptional3.isPresent()).isTrue();
        assertThat(mpaOptional3.get().getName()).isEqualTo("PG-13");

        Optional<MpaRating> mpaOptional4 = mpaRatingRepository.getMpaRatingById(4);
        assertThat(mpaOptional4.isPresent()).isTrue();
        assertThat(mpaOptional4.get().getName()).isEqualTo("R");

        Optional<MpaRating> mpaOptional5 = mpaRatingRepository.getMpaRatingById(5);
        assertThat(mpaOptional5.isPresent()).isTrue();
        assertThat(mpaOptional5.get().getName()).isEqualTo("NC-17");
    }

    @Test
    public void testGetMpaRatingByNotValidId() {
        Optional<MpaRating> mpaOptional = mpaRatingRepository.getMpaRatingById(999);
        assertThat(mpaOptional.isPresent()).isFalse();
    }

    @Test
    public void testMpaRatingOrderAndCompleteness() {
        List<MpaRating> allMpaRatings = mpaRatingRepository.findAll();

        // Проверяем, что все рейтинги присутствуют и имеют правильные ID
        boolean hasG = allMpaRatings.stream().anyMatch(mpa -> mpa.getId() == 1 && "G".equals(mpa.getName()));
        boolean hasPG = allMpaRatings.stream().anyMatch(mpa -> mpa.getId() == 2 && "PG".equals(mpa.getName()));
        boolean hasPG13 = allMpaRatings.stream().anyMatch(mpa -> mpa.getId() == 3 && "PG-13".equals(mpa.getName()));
        boolean hasR = allMpaRatings.stream().anyMatch(mpa -> mpa.getId() == 4 && "R".equals(mpa.getName()));
        boolean hasNC17 = allMpaRatings.stream().anyMatch(mpa -> mpa.getId() == 5 && "NC-17".equals(mpa.getName()));

        assertThat(hasG).isTrue();
        assertThat(hasPG).isTrue();
        assertThat(hasPG13).isTrue();
        assertThat(hasR).isTrue();
        assertThat(hasNC17).isTrue();
    }

    @Test
    public void testMpaRatingUniqueness() {
        List<MpaRating> allMpaRatings = mpaRatingRepository.findAll();

        // Проверяем уникальность ID
        long uniqueIds = allMpaRatings.stream()
                .map(MpaRating::getId)
                .distinct()
                .count();
        assertThat(uniqueIds == 5).isTrue();

        // Проверяем уникальность названий
        long uniqueNames = allMpaRatings.stream()
                .map(MpaRating::getName)
                .distinct()
                .count();
        assertThat(uniqueNames == 5).isTrue();
    }


    @Test
    public void testMpaRatingBoundaryCases() {
        // Проверяем граничные значения ID
        Optional<MpaRating> zeroId = mpaRatingRepository.getMpaRatingById(0);
        assertThat(zeroId.isPresent()).isFalse();

        Optional<MpaRating> negativeId = mpaRatingRepository.getMpaRatingById(-1);
        assertThat(negativeId.isPresent()).isFalse();

        Optional<MpaRating> maxId = mpaRatingRepository.getMpaRatingById(Integer.MAX_VALUE);
        assertThat(maxId.isPresent()).isFalse();
    }

}