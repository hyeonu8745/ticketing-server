package com.ticketing.server.repository;

import com.ticketing.server.domain.Reservation;
import com.ticketing.server.domain.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 기존
    List<Reservation> findAllByUserIdAndStatusOrderByReservedAtDesc(Long userId, ReservationStatus status);
    int countByUserIdAndStatus(Long userId, ReservationStatus status);

    // ──────────────────────────────────────────────
    // 🌟 관리자용
    // ──────────────────────────────────────────────

    // 모든 예매 조회 (상태/검색 필터)
    @Query("""
        SELECT r FROM Reservation r
        WHERE (:status IS NULL OR r.status = :status)
          AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(r.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(r.user.name)  LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(r.seat.event.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY r.reservedAt DESC
    """)
    Page<Reservation> findAllForAdmin(
            @Param("status") ReservationStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    long countByStatus(ReservationStatus status);

    // 누적 매출 (CONFIRMED만)
    @Query("SELECT COALESCE(SUM(r.seat.price), 0) FROM Reservation r WHERE r.status = :status")
    Long sumPriceByStatus(@Param("status") ReservationStatus status);

    // 특정 유저의 CONFIRMED 카운트
    long countByUserIdAndStatusEquals(Long userId, ReservationStatus status);

    // 인기 공연 TOP N (CONFIRMED 기준)
    @Query("""
        SELECT r.seat.event.id, r.seat.event.title, COUNT(r)
        FROM Reservation r
        WHERE r.status = com.ticketing.server.domain.ReservationStatus.CONFIRMED
        GROUP BY r.seat.event.id, r.seat.event.title
        ORDER BY COUNT(r) DESC
    """)
    List<Object[]> findTopEventsByReservation(Pageable pageable);

    // 일자별 예매 카운트 (CONFIRMED, 최근 N일)
    @Query("""
        SELECT FUNCTION('DATE', r.reservedAt), COUNT(r)
        FROM Reservation r
        WHERE r.reservedAt >= :since
          AND r.status = com.ticketing.server.domain.ReservationStatus.CONFIRMED
        GROUP BY FUNCTION('DATE', r.reservedAt)
        ORDER BY FUNCTION('DATE', r.reservedAt) ASC
    """)
    List<Object[]> findDailyCount(@Param("since") LocalDateTime since);

    // 카테고리별 공연 수 ↓ EventRepository에 두는 게 자연스럽지만 통계 한 곳에 모아두려 여기 둠
    @Query("""
        SELECT e.category.displayName, COUNT(e)
        FROM Event e
        WHERE e.visible = true
        GROUP BY e.category.displayName
        ORDER BY COUNT(e) DESC
    """)
    List<Object[]> countEventsByCategory();
}
