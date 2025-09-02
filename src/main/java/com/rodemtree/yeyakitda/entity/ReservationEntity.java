package com.rodemtree.yeyakitda.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "reservation")
public class ReservationEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantEntity restaurant;

    @ManyToOne
    @JoinColumn(name = "reservation_slot_id", nullable = false)
    private ReservationSlotEntity reservationSlot;

    @Column(name = "head_count", nullable = false)
    private Integer headCount;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Builder
    private ReservationEntity(UserEntity user, RestaurantEntity restaurant, ReservationSlotEntity reservationSlot, Integer headCount) {
        this.user = user;
        this.restaurant = restaurant;
        this.reservationSlot = reservationSlot;
        this.headCount = headCount;
    }

    public void setDefaultStatus() {
        this.status = ReservationStatus.WAITING;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}
