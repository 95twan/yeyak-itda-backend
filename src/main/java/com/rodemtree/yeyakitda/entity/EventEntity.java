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

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "banner_image_url", length = 256, nullable = false)
    private String bannerImageUrl;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    private EventEntity(String title,  String content, String bannerImageUrl, LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.content = content;
        this.bannerImageUrl = bannerImageUrl;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static EventEntity of(String title, String content, String bannerImageUrl,  LocalDateTime startDate, LocalDateTime endDate) {
        return new EventEntity(title, content, bannerImageUrl, startDate, endDate);
    }
}
