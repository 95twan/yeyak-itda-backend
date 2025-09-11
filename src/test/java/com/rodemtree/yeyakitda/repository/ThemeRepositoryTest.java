package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.config.JpaConfig;
import com.rodemtree.yeyakitda.entity.ThemeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("레포지토리 - 테마")
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        themeRepository.deleteAll();

        LocalDateTime now = LocalDateTime.now();

        // 1. 현재 진행 중인 테마
        themeRepository.save(createTheme("진행중인 테마", now.minusDays(1), now.plusDays(1)));
        // 2. 종료된 테마
        themeRepository.save(createTheme("종료된 테마", now.minusDays(10), now.minusDays(1)));
        // 3. 시작 전 테마
        themeRepository.save(createTheme("시작 전 테마", now.plusDays(1), now.plusDays(10)));
    }

    @Test
    @DisplayName("성공 - 현재 날짜 기준으로 진행 중인 테마만 조회한다.")
    void findInProgressThemesTest() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        List<ThemeEntity> result = themeRepository.findInProgressThemes(now);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("진행중인 테마");
    }

    private ThemeEntity createTheme(String title, LocalDateTime startDate, LocalDateTime endDate) {
        return ThemeEntity.of(title, startDate, endDate);
    }
}
