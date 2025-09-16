package com.rodemtree.yeyakitda.repository.mongodb;

import com.rodemtree.yeyakitda.document.RestaurantOperatingHours;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RestaurantOperatingHoursRepository extends MongoRepository<RestaurantOperatingHours, String> {
    Optional<RestaurantOperatingHours> findByRestaurantId(Long restaurantId);
}
