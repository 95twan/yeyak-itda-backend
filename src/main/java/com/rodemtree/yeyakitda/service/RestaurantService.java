package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.mapper.RestuarantMapper;
import com.rodemtree.yeyakitda.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestuarantMapper restuarantMapper;

    public Page<RestaurantDto> getRestaurantList(Set<String> categories, Pageable pageable) {
        Page<RestaurantEntity> restaurantEntityPage = restaurantRepository.findByCategories(categories, pageable);
        return restaurantEntityPage.map(restuarantMapper::restaurantEntityToRestaurantDto);
    }
}
