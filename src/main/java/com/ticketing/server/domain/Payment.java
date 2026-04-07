package com.ticketing.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation; // 어떤 예약에 대한 결제인가?

    private Long amount; // 결제 금액

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // READY, COMPLETED, CANCELLED

    private String paymentMethod; // 예: "MOCK_CARD", "POINT"

    @Builder
    public Payment(Reservation reservation, Long amount, PaymentStatus status) {
        this.reservation = reservation;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = "MOCK_CARD";
    }
}
