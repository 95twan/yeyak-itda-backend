package com.rodemtree.yeyakitda.repository.mongodb;

import com.rodemtree.yeyakitda.config.AbstractMongoDBContainer;
import com.rodemtree.yeyakitda.document.OperatingHour;
import com.rodemtree.yeyakitda.document.RestaurantOperatingHours;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataMongoTest
@ActiveProfiles("test")
@DisplayName("리포지토리 - RestaurantOperatingHours")
class RestaurantOperatingHoursRepositoryTest extends AbstractMongoDBContainer {
    @Autowired
    private RestaurantOperatingHoursRepository restaurantOperatingHoursRepository;

    @AfterEach
    void tearDown() {
        restaurantOperatingHoursRepository.deleteAll();
    }

    @Test
    @DisplayName("성공 - RestaurantOperatingHours 도큐먼트를 저장하고 restaurantId로 조회한다.")
    void saveAndFindByRestaurantIdTest() {
        // Given
        Long restaurantId = 1L;
        RestaurantOperatingHours operatingHours = createOperatingHours(restaurantId);

        // When
        restaurantOperatingHoursRepository.save(operatingHours);
        Optional<RestaurantOperatingHours> result = restaurantOperatingHoursRepository.findByRestaurantId(restaurantId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getRestaurantId()).isEqualTo(restaurantId);
        assertThat(result.get().getOperatingHours()).hasSize(1);
        assertThat(result.get().getOperatingHours().get(0).getDayOfWeek()).isEqualTo("수");

    }

    @Test
    @DisplayName("실패 - 존재하지 않는 restaurantId로 조회하면 빈 Optional을 반환한다.")
    void findByNonExistentRestaurantIdTest() {
        // Given
        Long nonExistentId = 999L;

        // When
        Optional<RestaurantOperatingHours> foundOperatingHours = restaurantOperatingHoursRepository.findByRestaurantId(nonExistentId);

        // Then
        assertThat(foundOperatingHours).isNotPresent();
    }


    private RestaurantOperatingHours createOperatingHours(Long restaurantId) {
        OperatingHour monday = OperatingHour.builder()
                .dayOfWeek("수")
                .isClosed(true)
                .build();

        return RestaurantOperatingHours.of(restaurantId, List.of(monday));
    }

}
