package com.ticketing.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // --- 추가된 부분 ---
    @Column(nullable = false)
    private Long point = 0L; // 기본값 0원

    // 포인트 충전 메서드
    public void chargePoint(Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0원보다 커야 합니다.");
        }
        this.point += amount;
    }

    // 포인트 차감 메서드
    public void usePoint(Long amount) {
        if (this.point < amount) {
            throw new RuntimeException("잔액이 부족합니다. 현재 잔액: " + this.point);
        }
        this.point -= amount;
    }
    // ------------------

    @Builder
    public User(String email, String name, String password, UserRole role, Long point) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role != null ? role : UserRole.ROLE_USER;
        this.point = point != null ? point : 0L; // 초기 포인트 설정 가능
    }

    public void updateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 비어있을 수 없습니다.");
        }
        this.name = name;
    }
}