package com.rodemtree.yeyakitda.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "restaurant")
public class RestaurantEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "name", length = 32, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thumbnail_image_id")
    private RestaurantImageEntity thumbnailImage;

    @Column(name = "description", length = 2000, nullable = false)
    private String description;

    @Column(name = "phone_number", length = 32, nullable = false)
    private String phoneNumber;

    @Column(name = "address", length = 128, nullable = false)
    private String address;

    @Column(name = "category", length = 16, nullable = false)
    private String category;

    @Column(name = "rating", nullable = false)
    private Float rating = 0f;

    @Builder
    private RestaurantEntity(
            UserEntity user,
            String name,
            String description,
            String phoneNumber,
            String address,
            String category
    ) {
        this.user = user;
        this.name = name;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.category = category;
    }

    public void updateRestaurantThumbnailImage(RestaurantImageEntity restaurantImage) {
        this.thumbnailImage = restaurantImage;
    }
}
