package com.rodemtree.yeyakitda.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "event")
public class EventEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "banner_image_url", length = 256, nullable = false)
    private String bannerImageUrl;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    private EventEntity(String title, String bannerImageUrl, String content, LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.bannerImageUrl = bannerImageUrl;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static EventEntity of(String title, String bannerImageUrl, String content, LocalDateTime startDate, LocalDateTime endDate) {
        return new EventEntity(title, bannerImageUrl, content, startDate, endDate);
    }

    public static EventEntity of(String title, String bannerImageUrl, String content) {
        return EventEntity.of(title, bannerImageUrl, content, null, null);
    }
}
