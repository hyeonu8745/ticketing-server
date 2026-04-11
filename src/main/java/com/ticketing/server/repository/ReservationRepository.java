package com.ticketing.server.repository;

import com.ticketing.server.domain.Reservation;
import com.ticketing.server.domain.ReservationStatus; // 추가 확인!
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 🌟 유저 ID로 찾되, '상태'가 특정값(CONFIRMED)인 것만 시간 역순으로 가져오기
    List<Reservation> findAllByUserIdAndStatusOrderByReservedAtDesc(Long userId, ReservationStatus status);
}