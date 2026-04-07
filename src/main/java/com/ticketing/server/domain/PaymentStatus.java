package com.ticketing.server.domain;

public enum PaymentStatus {
    READY,      // 결제 대기 (버튼 누르기 전)
    COMPLETED,  // 결제 완료 (승인됨)
    CANCELLED   // 결제 취소 (시간 초과 등)
}
