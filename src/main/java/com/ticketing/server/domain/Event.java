package com.ticketing.server.domain;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(columnDefinition = "TEXT")
    private String playTime;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String runtime;
    private String rating;
    private String cast;
    private String kopisEventId;

    private int totalSeats;

    // 🌟 신규: soft hide. 기본값 true. false면 일반 사용자에게 안 보임.
    @Column(nullable = false)
    private boolean visible = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seats = new ArrayList<>();

    @Builder
    public Event(String title, String location, String posterUrl, LocalDateTime startTime,
                 String playTime, String description, String runtime, String rating,
                 String cast, Category category, String kopisEventId, int totalSeats) {
        this.title = title;
        this.location = location;
        this.posterUrl = posterUrl;
        this.startTime = startTime;
        this.playTime = playTime;
        this.description = description;
        this.runtime = runtime;
        this.rating = rating;
        this.cast = cast;
        this.category = category;
        this.kopisEventId = kopisEventId;
        this.totalSeats = totalSeats;
        this.visible = true;
    }

    // 🌟 관리자: 공연 숨기기 / 다시 표시
    public void hide() { this.visible = false; }
    public void show() { this.visible = true; }

    // 🌟 관리자: 기본 정보 수정
    public void updateInfo(String title, String location, String description) {
        if (title != null && !title.isBlank()) this.title = title;
        if (location != null && !location.isBlank()) this.location = location;
        if (description != null) this.description = description;
    }
}
