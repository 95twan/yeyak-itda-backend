package com.rodemtree.yeyakitda.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "menu")
public class MenuEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantEntity restaurant;

    @Column(name = "name", length = 32, nullable = false)
    private String name;

    @Column(name = "description", length = 128, nullable = false)
    private String description;

    @Column(name = "image_url", length = 256, nullable = false)
    private String imageUrl;

    @Column(name = "price", nullable = false)
    private Integer price;

    private MenuEntity(RestaurantEntity restaurant, String name, String description, String imageUrl, Integer price) {
        this.restaurant = restaurant;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public static MenuEntity of(RestaurantEntity restaurant, String name, String description, String imageUrl, Integer price) {
        return new MenuEntity(restaurant, name, description, imageUrl, price);
    }
}
