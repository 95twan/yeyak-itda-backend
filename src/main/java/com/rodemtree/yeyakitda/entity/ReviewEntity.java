package com.rodemtree.yeyakitda.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "review")
public class ReviewEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantEntity restaurant;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "comment", length = 1000, nullable = false)
    private String comment;

    @Column(name = "rating", nullable = false)
    private Float rating = 0f;

    private ReviewEntity(RestaurantEntity restaurant, UserEntity user, String comment, Float rating) {
        this.restaurant = restaurant;
        this.user = user;
        this.comment = comment;
        this.rating = rating;
    }

    public static ReviewEntity of(RestaurantEntity restaurant, UserEntity user, String comment, Float rating) {
        return new ReviewEntity(restaurant, user, comment, rating);
    }
}
