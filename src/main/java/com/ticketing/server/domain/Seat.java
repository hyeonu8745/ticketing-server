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
    private Event event;

    private String seatNumber;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    // 🌟 추가: 좌석 가격 (단위: 원)
    @Column(nullable = false)
    private Long price;

    @Builder
    public Seat(Event event, String seatNumber, SeatStatus status, Long price) {
        this.event = event;
        this.seatNumber = seatNumber;
        this.status = status;
        // 가격이 안 들어오면 기본 50,000원으로 세팅
        this.price = (price != null) ? price : 50000L;
    }

    // --- 비즈니스 로직 ---

    public void reserve() {
        if (this.status == SeatStatus.RESERVED) {
            throw new IllegalStateException("이미 예약된 좌석입니다.");
        }
        this.status = SeatStatus.RESERVED;
    }

    public void cancel() {
        this.status = SeatStatus.AVAILABLE;
    }
}