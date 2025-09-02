package com.rodemtree.yeyakitda.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Min(1)
    @Max(5)
    @Column(name = "rating", nullable = false)
    private Integer rating = 0;

    private ReviewEntity(RestaurantEntity restaurant, UserEntity user, String comment, Integer rating) {
        this.restaurant = restaurant;
        this.user = user;
        this.comment = comment;
        this.rating = rating;
    }

    public static ReviewEntity of(RestaurantEntity restaurant, UserEntity user, String comment, Integer rating) {
        return new ReviewEntity(restaurant, user, comment, rating);
    }
}
