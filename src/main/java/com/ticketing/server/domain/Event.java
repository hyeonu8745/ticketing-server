package com.ticketing.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "events")
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String location;
    private String posterUrl;
    private LocalDateTime startTime;
    private int totalSeats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false) // FK 설정
    private Category category;

    @Column(columnDefinition = "TEXT")
    private String description; // 🌟 상세 설명 추가

    private String kopisEventId;

    // 🌟 좌석 수를 계산하기 위해 양방향 연관 관계 추가
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seats = new ArrayList<>();

    @Builder
    public Event(String title, String location, String posterUrl, LocalDateTime startTime,
                 int totalSeats, Category category, String description, String kopisEventId) {
        this.title = title;
        this.location = location;
        this.posterUrl = posterUrl;
        this.startTime = startTime;
        this.totalSeats = totalSeats;
        this.category = category;
        this.description = description;
        this.kopisEventId = kopisEventId;
    }
}