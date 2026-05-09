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

    private Long price;
    private String grade; // 🌟 등급 (VIP, R, S)

    @Builder
    public Seat(Event event, String seatNumber, SeatStatus status, Long price, String grade) {
        this.event = event;
        this.seatNumber = seatNumber;
        this.status = status;
        this.price = (price != null) ? price : 50000L;
        this.grade = (grade != null) ? grade : "일반석";
    }

    // --------------------------------------------------
    // 🌟 핵심 비즈니스 로직 (여기부터 추가/복구된 부분입니다)
    // --------------------------------------------------

    /**
     * 좌석 선점 (예매 시 호출)
     */
    public void reserve() {
        if (this.status == SeatStatus.RESERVED) {
            throw new IllegalStateException("이미 예약된 좌석입니다.");
        }
        this.status = SeatStatus.RESERVED;
    }

    /**
     * 좌석 해제 (취소 시 호출)
     */
    public void cancel() {
        this.status = SeatStatus.AVAILABLE;
    }
}