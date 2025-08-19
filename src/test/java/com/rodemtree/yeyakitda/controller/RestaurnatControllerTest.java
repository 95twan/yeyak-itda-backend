package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.config.TestSecurityConfig;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.service.RestaurantService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestaurnatController.class)
@Import(TestSecurityConfig.class)
class RestaurnatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestaurantService restaurantService;

    @Test
    @DisplayName("성공 - 식당 목록을 요청하면 기본 페이징(0페이지, 12개)된 식당 목록을 반환한다.")
    void getRestaurantListDefaultPaging() throws Exception {
        // Given
        given(restaurantService.getRestaurantList(any(Pageable.class))).willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 12), 0));

        // When & Then
        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pageInfo").exists())
                .andExpect(jsonPath("$.pageInfo.page").value(0))
                .andExpect(jsonPath("$.pageInfo.size").value(12));

        then(restaurantService).should().getRestaurantList(any(Pageable.class));
    }

    @Test
    @DisplayName("성공 - 페이지 정보와 식당 목록을 요청하면 페이징된 식당 목록을 반환한다.")
    void getRestaurantListWithPaging() throws Exception {
        // Given
        int page = 0;
        int size = 8;
        Pageable pageable = PageRequest.of(page, size);
        given(restaurantService.getRestaurantList(any(Pageable.class))).willReturn(new PageImpl<>(List.of(), pageable, 0));

        // When & Then
        mockMvc.perform(get("/api/restaurants")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageInfo.page").value(0))
                .andExpect(jsonPath("$.pageInfo.size").value(8));

        then(restaurantService).should().getRestaurantList(any(Pageable.class));
    }

    @Test
    @DisplayName("성공 - 정렬 정보와 식당 목록을 요청하면 정렬된 식당 목록을 반환한다.")
    void getRestaurantListWithSorting() throws Exception {
        // Given
        given(restaurantService.getRestaurantList(any(Pageable.class))).willReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/restaurants")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        then(restaurantService).should().getRestaurantList(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Test
    @Disabled
    @DisplayName("실패 - 존재하지 않는 필드로 정렬을 요청하면 400 Bad Request를 반환한다.")
    void getRestaurantListWithInvalidSortProperty() throws Exception {
        // Given
        String invalidSortParam = "invalidProperty";
        given(restaurantService.getRestaurantList(any(Pageable.class))).willThrow(new PropertyReferenceException(invalidSortParam, TypeInformation.of(RestaurantEntity.class), Collections.emptyList()));

        // When & Then
        mockMvc.perform(get("/api/restaurants")
                        .param("sort", invalidSortParam+",asc"))
                .andExpect(status().isBadRequest());
    }
}
