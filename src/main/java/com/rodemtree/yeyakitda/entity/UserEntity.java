package com.rodemtree.yeyakitda.entity;

import jakarta.persistence.*;
import lombok.*;


@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email", length = 64, unique = true, nullable = false)
    private String email;

    @Column(name = "password", length = 256, nullable = false)
    private String password;

    @Column(name = "name", length = 32, nullable = false)
    private String name;

    @Column(name = "nickname", length = 64, unique = true, nullable = false)
    private String nickname;

    @Column(name = "address", length = 128, nullable = false)
    private String address;

    @Column(name = "phone_number", length = 32, unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "role", length = 32, nullable = false)
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
