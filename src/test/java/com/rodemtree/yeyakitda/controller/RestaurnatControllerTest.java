package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.config.TestSecurityConfig;
import com.rodemtree.yeyakitda.dto.request.RestaurantSearchConditionDto;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
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
    void getRestaurantListWithDefaultPaging() throws Exception {
        // Given
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().build();
        given(restaurantService.getRestaurantList(eq(condition), any(Pageable.class))).willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 12), 0));

        // When & Then
        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.pageInfo").exists())
                .andExpect(jsonPath("$.data.pageInfo.page").value(0))
                .andExpect(jsonPath("$.data.pageInfo.size").value(12));

        then(restaurantService).should().getRestaurantList(eq(condition), any(Pageable.class));
    }

    @Test
    @DisplayName("성공 - 페이지 정보로 식당 목록을 요청하면 페이징된 식당 목록을 반환한다.")
    void getRestaurantListWithPaging() throws Exception {
        // Given
        int page = 0;
        int size = 8;
        Pageable pageable = PageRequest.of(page, size);
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().build();
        given(restaurantService.getRestaurantList(eq(condition), any(Pageable.class))).willReturn(new PageImpl<>(List.of(), pageable, 0));

        // When & Then
        mockMvc.perform(get("/api/restaurants")
                        .param("page", page + "")
                        .param("size", size + ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageInfo.page").value(0))
                .andExpect(jsonPath("$.data.pageInfo.size").value(8));

        then(restaurantService).should().getRestaurantList(eq(condition), any(Pageable.class));
    }

    @Test
    @DisplayName("성공 - 정렬 정보로 식당 목록을 요청하면 정렬된 식당 목록을 반환한다.")
    void getRestaurantListWithSorting() throws Exception {
        // Given
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().build();
        given(restaurantService.getRestaurantList(eq(condition), any(Pageable.class))).willReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/restaurants")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        then(restaurantService).should().getRestaurantList(eq(condition), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Test
    @Disabled
    @DisplayName("실패 - 존재하지 않는 필드로 정렬을 요청하면 400 Bad Request를 반환한다.")
    void getRestaurantListWithInvalidSortProperty() throws Exception {
        // Given
        String invalidSortParam = "invalidProperty";
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().build();
        given(restaurantService.getRestaurantList(eq(condition), any(Pageable.class))).willThrow(new PropertyReferenceException(invalidSortParam, TypeInformation.of(RestaurantEntity.class), Collections.emptyList()));

        // When & Then
        mockMvc.perform(get("/api/restaurants")
                        .param("sort", invalidSortParam+",asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공 - 카테고리로 식당 목록을 요청하면 해당 카테고리의 식당 목록을 반환한다.")
    void getRestaurantListWithCategories() throws Exception {
        // Given
        Set<String> expectedCategories = Set.of("한식", "중식");
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().categories(expectedCategories).build();
        given(restaurantService.getRestaurantList(eq(condition), any(Pageable.class))).willReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/restaurants")
                        .param("categories", "한식,중식"))
                .andExpect(status().isOk());

        ArgumentCaptor<RestaurantSearchConditionDto> captor = ArgumentCaptor.forClass(RestaurantSearchConditionDto.class);
        then(restaurantService).should().getRestaurantList(captor.capture(), any(Pageable.class));

        Set<String> categories = captor.getValue().categories();
        assertThat(categories).isEqualTo(expectedCategories);
    }

    @Test
    @DisplayName("성공 - 카테고리로 식당 목록을 요청하면 해당 카테고리의 식당 목록을 반환한다.")
    void getRestaurantListWithKeyword() throws Exception {
        // Given
        String exepectedKeyword = "테스트";
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().keyword(exepectedKeyword).build();
        given(restaurantService.getRestaurantList(eq(condition), any(Pageable.class))).willReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/restaurants")
                        .param("keyword", exepectedKeyword))
                .andExpect(status().isOk());

        ArgumentCaptor<RestaurantSearchConditionDto> captor = ArgumentCaptor.forClass(RestaurantSearchConditionDto.class);
        then(restaurantService).should().getRestaurantList(captor.capture(), any(Pageable.class));
        String keyword = captor.getValue().keyword();
        assertThat(keyword).isEqualTo(exepectedKeyword);
    }

    @Test
    @DisplayName("성공 - 카테고리로 식당 목록을 요청하면 해당 카테고리의 식당 목록을 반환한다.")
    void getRestaurantListWithCategoriesAndKeyword() throws Exception {
        // Given
        String exepectedKeyword = "테스트";
        Set<String> expectedCategories = Set.of("한식", "중식");
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().categories(expectedCategories).keyword(exepectedKeyword).build();
        given(restaurantService.getRestaurantList(eq(condition), any(Pageable.class))).willReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/restaurants")
                        .param("categories", "한식,중식")
                        .param("keyword", exepectedKeyword))
                .andExpect(status().isOk());

        ArgumentCaptor<RestaurantSearchConditionDto> captor = ArgumentCaptor.forClass(RestaurantSearchConditionDto.class);
        then(restaurantService).should().getRestaurantList(captor.capture(), any(Pageable.class));

        String keyword = captor.getValue().keyword();
        assertThat(keyword).isEqualTo(exepectedKeyword);
        Set<String> categories = captor.getValue().categories();
        assertThat(categories).isEqualTo(expectedCategories);
    }
}
