package com.ticketing.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "seats")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event; // 어떤 공연의 좌석인가?

    private String seatNumber; // 예: A-10

    @Enumerated(EnumType.STRING)
    private SeatStatus status; // AVAILABLE, RESERVED

    @Builder
    public Seat(Event event, String seatNumber, SeatStatus status) {
        this.event = event;
        this.seatNumber = seatNumber;
        this.status = status;
    }
}
