package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.config.TestSecurityConfig;
import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.dto.RestaurantGroupDto;
import com.rodemtree.yeyakitda.service.RestaurantGroupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestaurantGroupController.class)
@Import(TestSecurityConfig.class)
@DisplayName("컨트롤러 - 식당 그룹")
class RestaurantGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestaurantGroupService restaurantGroupService;

    @Test
    @DisplayName("성공 - 테마별로 그룹화된 식당 목록을 요청하면 200 OK와 함께 데이터를 반환한다.")
    void getThemeGroupedRestaurantsTest() throws Exception {
        // Given
        List<RestaurantDto> popularRestaurants = List.of(createRestaurantDto("빕스"));
        List<RestaurantDto> newRestaurants = List.of(createRestaurantDto("애슐리"));

        List<RestaurantGroupDto> restaurantGroupDtos = List.of(
                RestaurantGroupDto.builder().title("인기 식당").restaurants(popularRestaurants).build(),
                RestaurantGroupDto.builder().title("신규 식당").restaurants(newRestaurants).build()
        );

        given(restaurantGroupService.findThemeGroupedRestaurants()).willReturn(restaurantGroupDtos);

        // When & Then
        mockMvc.perform(get("/api/restaurant-groups")
                .param("group", "theme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공적으로 테마 별 식당을 조회했습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("인기 식당"))
                .andExpect(jsonPath("$.data[0].restaurants[0].name").value("빕스"));

        then(restaurantGroupService).should().findThemeGroupedRestaurants();
    }

    @Test
    @DisplayName("실패 - group=theme 파라미터 없이 요청하면 400 Bad Request를 반환한다.")
    void getThemeGroupedRestaurantsWithoutParamTest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/restaurant-groups")) // 파라미터 없이 요청
                .andExpect(status().isBadRequest()); // 400 에러를 기대
    }

    private RestaurantDto createRestaurantDto(String name) {
        return RestaurantDto.builder()
                .name(name)
                .build();
    }


}
