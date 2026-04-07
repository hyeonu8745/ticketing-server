package com.ticketing.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private LocalDateTime startTime;

    private int totalSeats;

    @Builder
    public Event(String title, LocalDateTime startTime, int totalSeats) {
        this.title = title;
        this.startTime = startTime;
        this.totalSeats = totalSeats;
    }
}

