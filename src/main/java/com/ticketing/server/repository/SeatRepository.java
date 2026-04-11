package com.ticketing.server.repository;

import com.ticketing.server.domain.Seat;
import com.ticketing.server.domain.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    // 1. [추가] 특정 공연의 전체 좌석 조회 (배치도 그릴 때 사용)
    List<Seat> findAllByEventId(Long eventId);

    // 2. 특정 공연의 특정 좌석 번호를 찾는 메서드
    Optional<Seat> findByEventIdAndSeatNumber(Long eventId, String seatNumber);

    // 3. 특정 공연의 '예매 가능한(AVAILABLE)' 좌석만 가져오는 메서드 (필터링용)
    List<Seat> findAllByEventIdAndStatus(Long eventId, SeatStatus status);
}