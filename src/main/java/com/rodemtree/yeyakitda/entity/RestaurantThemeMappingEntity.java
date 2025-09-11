package com.rodemtree.yeyakitda.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "restaurant_theme_mapping")
public class RestaurantThemeMappingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantEntity restaurant;

    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = false)
    private ThemeEntity theme;

    private RestaurantThemeMappingEntity(RestaurantEntity restaurant, ThemeEntity theme) {
        this.restaurant = restaurant;
        this.theme = theme;
    }

    public static RestaurantThemeMappingEntity of(RestaurantEntity restaurant, ThemeEntity theme) {
        return new RestaurantThemeMappingEntity(restaurant, theme);
    }
}
