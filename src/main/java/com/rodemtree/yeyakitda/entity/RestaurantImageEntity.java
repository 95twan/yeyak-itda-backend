package com.rodemtree.yeyakitda.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "restaurant_image")
public class RestaurantImageEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantEntity restaurant;

    @Column(name = "image_url", length = 256, nullable = false)
    private String imageUrl;

    private RestaurantImageEntity(RestaurantEntity restaurant, String imageUrl) {
        this.restaurant = restaurant;
        this.imageUrl = imageUrl;
    }

    public static RestaurantImageEntity of(RestaurantEntity restaurant, String imageUrl) {
        return new RestaurantImageEntity(restaurant, imageUrl);
    }
}
