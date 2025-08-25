package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.config.TestSecurityConfig;
import com.rodemtree.yeyakitda.dto.*;
import com.rodemtree.yeyakitda.dto.request.RestaurantSearchConditionDto;
import com.rodemtree.yeyakitda.dto.response.ResponseErrorCode;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.service.RestaurantService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestaurantController.class)
@Import(TestSecurityConfig.class)
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestaurantService restaurantService;

    @MockitoBean
    private Clock clock;

    @Test
    @DisplayName("성공 - 식당 목록을 요청하면 기본 페이징(0페이지, 12개)된 식당 목록을 반환한다.")
    void searchRestaurantsWithDefaultPagingTest() throws Exception {
        // Given
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().build();
        given(restaurantService.searchRestaurants(eq(condition), any(Pageable.class))).willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 12), 0));

        // When & Then
        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.pageInfo").exists())
                .andExpect(jsonPath("$.data.pageInfo.page").value(0))
                .andExpect(jsonPath("$.data.pageInfo.size").value(12));

        then(restaurantService).should().searchRestaurants(eq(condition), any(Pageable.class));
    }

    @Test
    @DisplayName("성공 - 페이지 정보로 식당 목록을 요청하면 페이징된 식당 목록을 반환한다.")
    void searchRestaurantsWithPagingTest() throws Exception {
        // Given
        int page = 0;
        int size = 8;
        Pageable pageable = PageRequest.of(page, size);
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().build();
        given(restaurantService.searchRestaurants(eq(condition), any(Pageable.class))).willReturn(new PageImpl<>(List.of(), pageable, 0));

        // When & Then
        mockMvc.perform(get("/api/restaurants")
                        .param("page", page + "")
                        .param("size", size + ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageInfo.page").value(0))
                .andExpect(jsonPath("$.data.pageInfo.size").value(8));

        then(restaurantService).should().searchRestaurants(eq(condition), any(Pageable.class));
    }

    @Test
    @DisplayName("성공 - 정렬 정보로 식당 목록을 요청하면 정렬된 식당 목록을 반환한다.")
    void searchRestaurantsWithSortingTest() throws Exception {
        // Given
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().build();
        given(restaurantService.searchRestaurants(eq(condition), any(Pageable.class))).willReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/restaurants")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        then(restaurantService).should().searchRestaurants(eq(condition), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 필드로 정렬을 요청하면 400 Bad Request를 반환한다.")
    void searchRestaurantsWithInvalidSortParamTest() throws Exception {
        // Given
        String invalidSortParam = "invalidProperty";
        given(restaurantService.searchRestaurants(any(RestaurantSearchConditionDto.class), any(Pageable.class)))
                .willThrow(new PropertyReferenceException(invalidSortParam, TypeInformation.of(RestaurantEntity.class), Collections.emptyList()));

        // When & Then
        mockMvc.perform(get("/api/restaurants")
                        .param("sort", invalidSortParam + ",asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("잘못된 정렬 기준입니다: '" + invalidSortParam + "'"));
    }

    @Test
    @DisplayName("성공 - 카테고리로 식당 목록을 요청하면 해당 카테고리의 식당 목록을 반환한다.")
    void searchRestaurantsWithCategoriesTest() throws Exception {
        // Given
        Set<String> expectedCategories = Set.of("한식", "중식");
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().categories(expectedCategories).build();
        given(restaurantService.searchRestaurants(eq(condition), any(Pageable.class))).willReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/restaurants")
                        .param("categories", "한식,중식"))
                .andExpect(status().isOk());

        ArgumentCaptor<RestaurantSearchConditionDto> captor = ArgumentCaptor.forClass(RestaurantSearchConditionDto.class);
        then(restaurantService).should().searchRestaurants(captor.capture(), any(Pageable.class));

        Set<String> categories = captor.getValue().categories();
        assertThat(categories).isEqualTo(expectedCategories);
    }

    @Test
    @DisplayName("성공 - 키워드로 식당 목록을 요청하면 해당 카테고리의 식당 목록을 반환한다.")
    void searchRestaurantsWithKeywordTest() throws Exception {
        // Given
        String exepectedKeyword = "테스트";
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().keyword(exepectedKeyword).build();
        given(restaurantService.searchRestaurants(eq(condition), any(Pageable.class))).willReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/restaurants")
                        .param("keyword", exepectedKeyword))
                .andExpect(status().isOk());

        ArgumentCaptor<RestaurantSearchConditionDto> captor = ArgumentCaptor.forClass(RestaurantSearchConditionDto.class);
        then(restaurantService).should().searchRestaurants(captor.capture(), any(Pageable.class));
        String keyword = captor.getValue().keyword();
        assertThat(keyword).isEqualTo(exepectedKeyword);
    }

    @Test
    @DisplayName("성공 - 카테고리와 키워드로 식당 목록을 요청하면 해당 카테고리의 식당 목록을 반환한다.")
    void searchRestaurantsWithCategoriesAndKeywordTest() throws Exception {
        // Given
        String exepectedKeyword = "테스트";
        Set<String> expectedCategories = Set.of("한식", "중식");
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().categories(expectedCategories).keyword(exepectedKeyword).build();
        given(restaurantService.searchRestaurants(eq(condition), any(Pageable.class))).willReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/restaurants")
                        .param("categories", "한식,중식")
                        .param("keyword", exepectedKeyword))
                .andExpect(status().isOk());

        ArgumentCaptor<RestaurantSearchConditionDto> captor = ArgumentCaptor.forClass(RestaurantSearchConditionDto.class);
        then(restaurantService).should().searchRestaurants(captor.capture(), any(Pageable.class));

        String keyword = captor.getValue().keyword();
        assertThat(keyword).isEqualTo(exepectedKeyword);
        Set<String> categories = captor.getValue().categories();
        assertThat(categories).isEqualTo(expectedCategories);
    }

    @Test
    @DisplayName("실패 - 검색 조건에 맞는 식당 목록이 없는 경우 빈 리스트의 content와 200 OK를 반환한다.")
    void searchRestaurantsNotExistTest() throws Exception {
        // Given
        String exepectedKeyword = "테스트";
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().keyword(exepectedKeyword).build();
        given(restaurantService.searchRestaurants(eq(condition), any(Pageable.class))).willReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/restaurants")
                        .param("keyword", exepectedKeyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("검색 결과가 없습니다."))
                .andExpect(jsonPath("$.data.content").value(empty()))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(0));
    }

    @Test
    @DisplayName("성공 - 식당 ID로 상세 조회를 요청하면, 200 OK와 함께 식당 상세 DTO를 반환한다.")
    void getRestaurantDetail() throws Exception {
        // Given
        Long restaurantId = 1L;
        RestaurantDetailDto restaurantDetailDto = createRestaurantDetailDto(restaurantId);
        given(restaurantService.getRestaurantDetail(eq(restaurantId), any())).willReturn(restaurantDetailDto);

        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        Instant fixedInstant = LocalDate.of(2025, 8, 25).atStartOfDay(seoulZone).toInstant();
        given(clock.instant()).willReturn(fixedInstant);
        given(clock.getZone()).willReturn(seoulZone);

        // When & Then
        mockMvc.perform(get("/api/restaurants/" + restaurantId)
                        .param("date", "2025-08-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.restaurant").isNotEmpty())
                .andExpect(jsonPath("$.data.reservationSlots").isArray())
                .andExpect(jsonPath("$.data.menus").isArray())
                .andExpect(jsonPath("$.data.reviews").isArray());

        then(restaurantService).should().getRestaurantDetail(eq(restaurantId), any());
    }

    @Test
    @DisplayName("실패 - 없는 식당 ID로 상세 조회를 요청하면, 404 NotFound를 응답한다.")
    void getRestaurantDetailWithNotExistRestaurantId() throws Exception {
        // Given
        Long restaurantId = 999L;
        given(restaurantService.getRestaurantDetail(eq(restaurantId), any())).willThrow(new EntityNotFoundException());

        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        Instant fixedInstant = LocalDate.of(2025, 8, 25).atStartOfDay(seoulZone).toInstant();
        given(clock.instant()).willReturn(fixedInstant);
        given(clock.getZone()).willReturn(seoulZone);

        // When & Then
        mockMvc.perform(get("/api/restaurants/" + restaurantId)
                        .param("date", "2025-08-26"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(ResponseErrorCode.RESOURCE_NOT_FOUND.getStatus()))
                .andExpect(jsonPath("$.message").value(ResponseErrorCode.RESOURCE_NOT_FOUND.getMessage()));

        then(restaurantService).should().getRestaurantDetail(eq(restaurantId), any());
    }

    @Test
    @DisplayName("실패 - date 파라미터가 없으면, 400 BadReqeust를 응답한다.")
    void getRestaurantDetailWithoutDate() throws Exception {
        // Given
        Long restaurantId = 1L;

        // When & Then
        mockMvc.perform(get("/api/restaurants/" + restaurantId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("date")));

        then(restaurantService).should(never()).getRestaurantDetail(eq(restaurantId), any());
    }

    @Test
    @DisplayName("실패 - 과거 날짜로 상세 조회를 요청하면, 400 BadReqeust를 응답한다.")
    void getRestaurantDetailWithPastDate() throws Exception {
        // Given
        Long restaurantId = 1L;

        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        Instant fixedInstant = LocalDate.of(2025, 8, 25).atStartOfDay(seoulZone).toInstant();
        given(clock.instant()).willReturn(fixedInstant);
        given(clock.getZone()).willReturn(seoulZone);

        // When & Then
        mockMvc.perform(get("/api/restaurants/" + restaurantId)
                        .param("date", "2025-08-24"))
                .andExpect(status().isBadRequest());

        then(restaurantService).should(never()).getRestaurantDetail(eq(restaurantId), any());
    }

    private RestaurantDetailDto createRestaurantDetailDto(Long id) {
        RestaurantInfoDto info = new RestaurantInfoDto(id, 1L, "테스트 식당", List.of(), "설명", List.of(), "주소", "한식", "010-1234-5678", 4.5f, List.of());
        List<ReservationSlotDto> reservationSlots = List.of(new ReservationSlotDto(1L, LocalDateTime.now(), 3));
        List<MenuDto> menus = List.of(new MenuDto(1L, "메뉴1", "설명1", 10000, "https://example.com/menu1.jpg"));
        List<ReviewDto> reviews = List.of(new ReviewDto(1L, 1L, "닉네임", List.of(), "코멘트", 5.0f));
        return new RestaurantDetailDto(info, reservationSlots, menus, reviews);
    }
}
