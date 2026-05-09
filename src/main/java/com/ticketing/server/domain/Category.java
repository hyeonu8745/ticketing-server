package com.ticketing.server.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // 식별 코드 (예: CONCERT, MUSICAL)

    private String displayName; // 화면 표시용 (예: 콘서트, 뮤지컬)

    @Builder // 🌟 빌더 패턴 추가
    public Category(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }
}