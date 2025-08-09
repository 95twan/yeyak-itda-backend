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

    private String email;

    private String password;

    private String name;

    private String nickname;

    private String address;

    private String phoneNumber;

    private String role;

    @Builder
    private UserEntity(String email, String password, String name, String nickname, String address, String phoneNumber) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }
}
