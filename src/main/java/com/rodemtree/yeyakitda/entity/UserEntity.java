package com.rodemtree.yeyakitda.entity;

import jakarta.persistence.*;
import lombok.*;


@Getter
@NoArgsConstructor
@Entity
public class UserEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String address;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Builder
    private UserEntity(String email, String password, String name, String nickname, String address, String phoneNumber) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public void setDefaultRole() {
        this.role = UserRole.ROLE_USER;
    }
}
