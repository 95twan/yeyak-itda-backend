package com.rodemtree.yeyakitda.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "review_image")
public class ReviewImageEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private ReviewEntity review;

    @Column(name = "image_url", length = 256, nullable = false)
    private String imageUrl;

    private ReviewImageEntity(ReviewEntity review, String imageUrl) {
        this.review = review;
        this.imageUrl = imageUrl;
    }

    public static ReviewImageEntity of(ReviewEntity review, String imageUrl) {
        return new ReviewImageEntity(review, imageUrl);
    }
}
