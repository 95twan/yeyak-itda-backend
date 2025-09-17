package com.rodemtree.yeyakitda.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_slot")
@Getter
@NoArgsConstructor
public class ReservationSlotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantEntity restaurant;

    @Column(name = "slot_at", nullable = false)
    private LocalDateTime slotAt;

    @Column(name = "total_capacity", nullable = false)
    private Integer totalCapacity;

    @Column(name = "reserved_capacity", nullable = false)
    private Integer reservedCapacity;

    private ReservationSlotEntity(RestaurantEntity restaurant, LocalDateTime slotAt, Integer totalCapacity, Integer reservedCapacity) {
        this.restaurant = restaurant;
        this.slotAt = slotAt;
        this.totalCapacity = totalCapacity;
        this.reservedCapacity = reservedCapacity;
    }

    public static ReservationSlotEntity of(RestaurantEntity restaurant, LocalDateTime slotAt, Integer totalCapacity, Integer reservedCapacity) {
        return new ReservationSlotEntity(restaurant, slotAt, totalCapacity, reservedCapacity);
    }

    public void addReservedCapacity(Integer capacity) {
        this.reservedCapacity += capacity;
    }

    public void removeReservedCapacity(Integer capacity) {
        this.reservedCapacity -= capacity;
    }

    public int getRemainingCapacity() {
        return this.totalCapacity - this.reservedCapacity;
    }

    public boolean isPossibleToReserve(Integer capacity) {
        return this.getRemainingCapacity() >= capacity;
    }

}
