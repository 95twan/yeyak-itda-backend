package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.mapper.RestuarantMapper;
import com.rodemtree.yeyakitda.repository.RestaurantRepository;
import com.rodemtree.yeyakitda.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final RestuarantMapper restuarantMapper;

    public Page<RestaurantDto> getRestaurantList(Pageable pageable) {
        Page<RestaurantEntity> restaurantEntityPage = restaurantRepository.findAll(pageable);
        return restaurantEntityPage.map(restuarantMapper::restaurantEntityToRestaurantDto);
    }
}
