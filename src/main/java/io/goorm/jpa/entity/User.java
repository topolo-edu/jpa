package io.goorm.jpa.entity;

import io.goorm.jpa.entity.common.BaseEntity;
import io.goorm.jpa.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_no")
    @EqualsAndHashCode.Include
    private Long userNo;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(length = 20)
    private String phone;

    @Column(length = 200)
    private String address;

    @Column(length = 500)
    private String bio;

    @Builder
    public User(String username, String password, String email, String fullName, UserRole role, String phone, String address, String bio) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.phone = phone;
        this.address = address;
        this.bio = bio;
    }

    // 기존 코드 호환성을 위한 생성자 (phone, address, bio는 null)
    public User(String username, String password, String email, String fullName, UserRole role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.phone = null;
        this.address = null;
        this.bio = null;
    }

    public void updateProfile(String email, String fullName, String phone, String address, String bio) {
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.bio = bio;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public boolean isInstructor() {
        return this.role == UserRole.INSTRUCTOR;
    }

    public boolean isStudent() {
        return this.role == UserRole.STUDENT;
    }
}
