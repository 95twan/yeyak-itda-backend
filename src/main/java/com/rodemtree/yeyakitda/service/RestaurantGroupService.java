package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.dto.RestaurantGroupDto;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.ThemeEntity;
import com.rodemtree.yeyakitda.mapper.RestaurantGroupMapper;
import com.rodemtree.yeyakitda.mapper.RestaurantMapper;
import com.rodemtree.yeyakitda.repository.RestaurantRepository;
import com.rodemtree.yeyakitda.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantGroupService {

    private final ThemeRepository themeRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantGroupMapper restaurantGroupMapper;
    private final RestaurantMapper restaurantMapper;
    private final Clock clock;

    @Cacheable("themeGroupedRestaurants")
    public List<RestaurantGroupDto> findThemeGroupedRestaurants() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<ThemeEntity> themes = themeRepository.findInProgressThemes(now);

        return themes.stream().map(theme -> {
            List<RestaurantEntity> restaurantEntities = restaurantRepository.findTop10ByTheme(theme);
            List<RestaurantDto> restaurantDtos = restaurantMapper.restaurantEntitiesToRestaurantDtos(restaurantEntities);
            return restaurantGroupMapper.group(theme, restaurantDtos);
        }).toList();
    }
}
