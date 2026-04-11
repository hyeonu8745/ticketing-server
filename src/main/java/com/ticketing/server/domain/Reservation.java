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
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    private LocalDateTime reservedAt;

    // 🌟 1. 예약 상태 추가 (기본값: CONFIRMED)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @Builder
    public Reservation(User user, Seat seat, LocalDateTime reservedAt, ReservationStatus status) {
        this.user = user;
        this.seat = seat;
        this.reservedAt = reservedAt;
        // 🌟 2. 상태값이 넘어오면 설정, 없으면 기본값 유지
        this.status = (status != null) ? status : ReservationStatus.CONFIRMED;
    }

    // 🌟 3. 소프트 딜리트를 위한 상태 변경 메서드
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public void updateSeat(Seat newSeat) {
        this.seat = newSeat;
    }
}